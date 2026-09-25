package dslabs.clientserver;

import dslabs.framework.Message;
import lombok.Data;
import dslabs.framework.Command;
import dslabs.framework.Result;

@Data
class Request implements Message {
    private final Command command;
    // Your code here...
    private final int sequenceNumber;

    public Request(Command command, int sequenceNumber) {
        this.command = command;
        this.sequenceNumber = sequenceNumber;
    }

    public Command command() {
        return this.command;
    }

    public int sequenceNumber() {
        return this.sequenceNumber;
    }
}

/* Respuesta del servidor al cliente. Nos dice la respuesta
* que el servidor le regresa al cliente tras haber procesado
* la peticion que el servidor le mando.  */
@Data
class Reply implements Message {
    private final Result result;
    // Your code here...
    private final int sequenceNumber;

    public Reply(Result result, int sequenceNumber) {
        this.result = result;
        this.sequenceNumber = sequenceNumber;
    }

    public Result result() {
        return this.result;
    }

    public int sequenceNumber() {
        return this.sequenceNumber;
    }
}


/* ********************************************************************
 * Esta clase define las estructuras de datos (mensajes)
 * que viajaran a traves de la red. Cualquier objeto que
 * implemente Message se convierte en serializable que
 * simuladamente vamos a enviar por la red de un nodo a
 * otro con su destino. */

/* La petición la manda el cliente y la recibe el servidor.
 * Va del cliente al servidor.  Nos dice lo que el cliente
 * quiere que el servidor haga. Request = solicitud/ peticion.
 * Esta clase funciona como un contenedor para transportar info
 * a traves de la red que simulamos desde el cliente hasta el
 * servidor. No es un metodo, es un obejeto de datos. El command
 * es la instruccion de la base de datos que tenemos en KVStore
 * y que puede ser del tipo put, get o append. Sin embargo, la
 * peticion/request no se guarda en la KVStore (pues esta es la
 * base de los datos del usuario, no los mensajes de red). */

/* - Request es la CARTA (el sobre cerrado). Solo guarda datos adentro:
 * el comando y el ticket de secuencia.
 * - Una carta NO ejecuta nada, NO tiene una base de datos adentro y NO
 * envía mensajes.
 * -KVStore es el ALMACEN (la base de datos). Vive dentro del Servidor.
 * KVStore vive como un atributo dentro de SimpleServer. El servidor
 * tiene UNA sola base de datos persistente para todas las peticiones
 * que le lleguen
 * -SimpleServer es el RECEPTOR / MENSAJERO. El recibe la carta, la abre,
 * llama a KVStore, crea la carta de respuesta (Reply) y la envia de vuelta.
 */

