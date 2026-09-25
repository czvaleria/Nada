package dslabs.clientserver;

import dslabs.atmostonce.AMOCommand;
import dslabs.framework.*;
import dslabs.kvstore.KVStore;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * <p>See the documentation of {@link Client} and {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)

/* El cliente es el que debe iniciar la comunicacion, lidiar con la perdida de paquetes
* (con ayuda del timer) y guardar el resultado para cuando la aplicacion principal
* se lo pida. El cliente es el puente entre el usuaro y el servidor. */
class SimpleClient extends Node implements Client {
  private final Address serverAddress;

  // Your code here...
  private Command command;
  private Result result;
  private int sequenceNumber = 0;
  private  Request currentRequest;

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public SimpleClient(Address address, Address serverAddress) {
    super(address);
    this.serverAddress = serverAddress;
  }

  @Override
  public synchronized void init() {
    // No initialization necessary
  }

  /* -----------------------------------------------------------------------------------------------
   *  Client Methods
   * ---------------------------------------------------------------------------------------------*/
  @Override
  public synchronized void sendCommand(Command command) {
    // Your code here...
    this.command = command;
    // this.result = null;
    this.sequenceNumber++;

    AMOCommand amoCommand = new AMOCommand(command, this.sequenceNumber, this.address());
    this.currentRequest = new Request(amoCommand, this.sequenceNumber);
    send(this.currentRequest, serverAddress);
    set(new ClientTimer(this.sequenceNumber), ClientTimer.CLIENT_RETRY_MILLIS);
  }

  @Override
  public synchronized boolean hasResult() {
    // Your code here...
    return this.result != null;
  }

  @Override
  public synchronized Result getResult() throws InterruptedException {
    // Your code here...
    while(!hasResult()){
        this.wait();
    }
    return this.result;
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void handleReply(Reply m, Address sender) {
    // Your code here...
    if(m.sequenceNumber() == this.sequenceNumber){
      this.result = m.result();
      notify();
    }
  }

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void onClientTimer(ClientTimer t) {
    // Your code here...
    /* Request rActual = this.request;
    Request rTimer = t.request();
    if(!hasResult() && rActual != null && rActual.equals(rTimer)){
      this.send(rActual, this.serverAddress);
      this.set(t, ClientTimer.CLIENT_RETRY_MILLIS);
    }
    if(this.lastResult != null) return;*/

    if(t.sequenceNumber() == this.sequenceNumber && !hasResult()){
      send(this.currentRequest, this.serverAddress);
      set(new ClientTimer(this.sequenceNumber), ClientTimer.CLIENT_RETRY_MILLIS);
    }
  }
}

/*  ***********************************************************************************************
* Command es la instruccion. El cliente le dice en el command a la base de datos lo que quiere que
* haga, puede ser put, get o append. Sus atributos dependen del tipo de comando. Si es get solo tiene
* key(), si es put o append tiene key() y value(). El cliente no los lee, solo los transporta de forma
* generica como un objeto de tipo Command.
*
* Result es el contendio de la respuesta. Es la respuesta cruda de la base de datos, como putOK() o
* KeyNotFound(). No sabe nada de redes, ni direcciones IP ni numero de secuencia.
*
* Reply es la carta que lleva a Result. Es el paquete que viajo por la red desde el servidor hasta el
* cliente. Sus atributos son Result de la base de datos y el numero de secuencia.
*
* Request es la clase del mensaje que el cliente envia al servidor. Implementa Message. Lo que hace
* es llevar el command y el sequenceNum. Puede hacer command() para devolver el command, sequencenNum()
* para devolver el int del numero de secuencia. No conoce Adress del serivor, pero la direccion del servidor
* conoce el cliente a traves de su this.serverAdress.
*
* ClientTimer implementa la interfaz Timer. Lo que hace es encapsular la peticion Request que dio origen
* a la alarma. Tiene request() para devolver el objeto Request almacenado dentro del timer. No tiene metodos
* propios, si no que .sequeceNum(), .message() o .address() lo consulta a traves del Request que trae dentro.
* */

/* sendCommand():
 * Primero incrementaos el contador de secuencia, como diciendo que el paquete que llego tiene
 * un turno +1 mas que el anterior. Luego guardamos la peticion en el atributo global. Luego
 * reseteamos el resultado previo (el del paquete anterior del turno anterior a nosotros, por asi
 * decirlo) a null. Luego enviamos la peticion de nuestra peticion. Luego iniciamos el timer
 * respectivo a nuestra peticion.  */

/* getResult():
* Es un metodo sincrono/ bloqueante. Cuando el cliente aun no tiene el result del servidor (osea
* this.result == null), entonces debemos esperar con this.wait() para asi poder preguntar por el
* result. Si otro hilo interrumoe a ese hilo mientras esta esperando, lanzamos la IE.  */

/* handleReplay():
 * Debe verificar que el sequenceNumber de Reply m (m es el mensaje de respuesta, que dentro trae
 * m.result y m.sequenceNumber) sea igual al sequenceNumber de nuestra peticion actual que estamos
 * trabajando.  Si no es igual, es que es menor o mayor a nuestro sequenceNumber, osea es un ticket
 * viejo o duplicado porque la red esta lenta o algo le paso, asi que solo lo ignoramos. Si si es
 * igual, guardamos el resultado de m y por lo tanto hasResult() = true y por lo tanto despertamos
 * al hilo (de hasResult()) que esta esperando a que ya haya un result y pues ya lo hay.  */

/*
* onClientTimer():
* Es una alarma de despertador cuando la red pierde un paquete. En sendCommand() hicimos con
* this.set(new ClientTimer(this.request), ClientTimer.CLIENT_RETRY_MILLIS); programas un reloj de
* cuenta regresiva que si llega a cero y el serivodor no responde, entonces se ejecuta este metodo
* onClientTimer(), el cual va a reintentar la peticion. Lo primero que hace es verificar que la
* alarma siga siendo valida, osea antes de reenviar a lo loco vamos a ver si de verdad necesitamos
* reenviar el paquete. Reenviamos un paquete si y solo si aun no tenemos resultado (osea no llego)
* Y el request del timer es igual a la peticion actual que estamos esperando (osea que ya llego y
* es el que estamos viendo). Solo reenviamos cuando el timer que acaba de somnar pertenece al request
* que estamos esperando actualmente. Osea queremos que la peticion del timer sea igual a la peticion
* actual para poder reenviar, Luego hacemos el reintento: reenviamos la peticion y reiniciamos la alarma.
 * */