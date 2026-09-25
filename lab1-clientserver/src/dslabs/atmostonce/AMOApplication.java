package dslabs.atmostonce;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class AMOApplication<T extends Application> implements Application {
  @Getter
  @NonNull
  private final T application;

  // Your code here...
  private Map<Address, AMOResult> historial = new HashMap<>();

  @Override
  public AMOResult execute(Command command) {
    if (!(command instanceof AMOCommand)) {
      throw new IllegalArgumentException();
    }

    AMOCommand amoCommand = (AMOCommand) command;

    // Your code here...
    if(alreadyExecuted(amoCommand)){
        return historial.get(amoCommand.clientAddress());
    }
    Result resultSubyacente = application.execute(amoCommand.command());
    AMOResult amoResult = new AMOResult(amoCommand.sequenceNumber(), resultSubyacente);
    historial.put(amoCommand.clientAddress(), amoResult);

    return amoResult;

    /* Result amoCommand = application.execute(amoCommand.command());
    AMOResult amoResult = new AMOResult(amoCommand.sequenceNumber(), amoCommand);
    // historial.put(amoCommand.clientAddress(), amoResultNew);
    saveResult(amoCommand, amoResult)
    return  amoResultNew;*/
  }

  public Result executeReadOnly(Command command) {
    if (!command.readOnly()) {
      throw new IllegalArgumentException();
    }

    if (command instanceof AMOCommand) {
      return execute(command);
    }

    return application.execute(command);
  }

  public boolean alreadyExecuted(AMOCommand amoCommand) {
    // Your code here...
    Address addresNew = amoCommand.clientAddress();
    // int seqNumNew = amoCommand.sequenceNumber();

    if (!historial.containsKey(addresNew)) return false;

    // AMOResult ultimoResult = historial.get(addresNew);
    // int seqNumOld = historial.get(addresNew).sequenceNumber();
    //int seqNumOld = ultimoResult.sequenceNumber();
    // int seqNumNew = amoCommand.sequenceNumber();

    // return  (seqNumNew <= seqNumOld);
    return (amoCommand.sequenceNumber() <= historial.get(addresNew).sequenceNumber());
  }
}
  /* alreadyExecuted(): Adddress es la key para buscar en el mapa la direccion del
  * cliente. Primero se obtiene la clave y ticket del paquete entrante, del cual
  * guardamos su direccion y el ticket (seqNum) que vienen adentro del amoCommand.
  * Luego verificamos si el cliente existe en el mapa historial:
  * - Si no esta en el historia/libreta/mapa significa que es un cliente nuevo, por
  * lo tanto nunca hemos ejecutado nada para el.
  * - Si el cliente si esta. Comparamos tickets: Primero obtenemos la ultima respuesta
  * guardada en el mapa, y le pedimos su ticket respectivo. Entonces:
  *   - Si nuevoTicket <= ultimoTicket, significa que el paquete entrante trae un
  * numero igual o mas viejo que el ulitmo que procesamos. Es decir, el paquete entrante
  * es <= que el ticket guardado, lo que signfica que es un paquete viejo o repetido.
  * Por lo tanto, ya fue procesado/ejecutado. .
  *   - De lo contario, es un numero mayor. Por lo tanto, es un nuevo paquete y
  *   entonces no se ha ejecutado. */


/* AMO es una capa envolvente (wrapper) que se coloca alrededor del servidor para
* darle memoria y evitar ejecuciones duplicadas.
*
* Podemos ver al servidor como un cajero automatico. El cajero anota el numero de
* ticket y la direccion de cada cliente que ya atendio, junto con el resultado que
* le dio. Si le llega un comando repetido (osea un numero de ticket que ya atendio),
* entonces el servidor no vuelve a ejecutar el comando en la base de datos, si no
* que simplemente busca en su directorio la respuesta anterior y la regresa.
*
* Podemos ver a T application como la base de datos pura (la kvStore), que por si sola
* ejecuta todo lo que se le pida sin hacer pregunta. AMOApplication es un wrapper
* (envuelve) decorator, que se coloca "alrededor" de la application para interceptar
* cada comando antes de que toque la base de datos real. Entonces el historia/ bitacora/
* libreta tiene la funcion de mantener el historial en memoria para poder pregunarle:
* 1) si ya ejecuto un comando x antes, 2) si ya lo ejecuto, cual fue el AMOResult que
* dio esa vez, y 3) si es nuevo, como actualizo el historial despues de ejecutarlo en
* application?
*
* AMORequest envuelve el command og de la app y le añade la direccion del cliente
* (ClientAdress) y el numero de ticket (sequenceNum). Ademas, envuelve al Result og
* de la base de datos, añadiendole tambien su numero de secuencia para que el cliente
* sepa a que peticion responde.
*
* AMOServer guarda un estado interno para recordar que peticiones ya proceso. Es decir,
* usa una estructura de datos donde mapea el cliente con tal direccion con su numero de
* ticket.
*
* Ciclo de vida: Primero lee lo que el cliente mando y que numero de ticket trae.
* - si el ticker es mas nuevo de lo que tenia registrado (ticketNuevo > ticketRegistrado)
* significa que la peticion es totalmente nueva (recordemos que aumenta de 1 en 1).
* Entonces se manda la peticion a ejecutar a la base de datos, guarda el resultado y
* actualiza el numero de ticket del cliente y responde.
* - si el ticket es exactamente al que ya proceso antes (ticketNuevo < ticketRegistrado)
* significa que es ya lo habian enviado y es un reenvio por culpa de un tiemout de la red.
* Entonces el servidor AMO no ejecuta la base de datos, si no que solo saca el resultado
* correspondiente y se lo reenvia al cliente.
* - si el ticket es viejo o atrasado, se ignora. */
