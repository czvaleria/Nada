package dslabs.clientserver;

import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import dslabs.atmostonce.AMOCommand;

/**
 * Simple server that receives requests and returns responses.
 *
 * <p>See the documentation of {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleServer extends Node {
  // Your code here...
  private final Application app;

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public SimpleServer(Address address, Application app) {
    super(address);

    // Your code here...
    this.app = app;
  }

  @Override
  public void init() {
    // No initialization necessary
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/

  private void handleRequest(Request m, Address sender) {
    // Your code here...
    // AMOCommand amoCommand = new AMOCommand(m.command(), m.sequenceNumber(), sender);
    AMOResult amoResult = (AMOResult) app.execute(m.command());
    // Result result = app.execute(m.command());
    Reply reply = new Reply(amoResult.result(), m.sequenceNumber());
    // if(result instanceof AMOResult){
      // result = ((AMOResult) result).result();
    // }

    // Reply reply = new Reply(amoResult.result(), m.sequenceNumber());
    //Reply reply = new Reply(result, m.sequenceNumber());
    send(reply, sender);
  }
}

/* ***********************************************************************************************
 * ¿Que hace el servidor cuando recibe un Request?
 * 1) Abre el request y extrae el command;
 * 2) se le pasa a la base de datos kvstore para que lo procese y lo
 * ejecute (puede ser put, get o append);
 * 3) prepara la respuesta replay, osea guarda el resultado (el que
 * nos dio procesar por la base de datos el comando) y le crea un
 * numero de secuencia (el cual el cliente inventa y va incrementando
 * en 1 cada vez que quiera enviar un comando nuevo. El servidor
 * copia exactamente el mismo sequenceNum tipo int de la solicitud en la
 * respuesta para que el cliente sepa a que "paquete" corresponde la
 * respuesta que le acaba de llegar) para meterla en una estructura
 * de datos Reply;
 * 4) enviamos el replay al cliente con send(msj, direccion) del servidor
 * que tenemos de SimpleServer. */

