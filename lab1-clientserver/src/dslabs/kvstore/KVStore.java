package dslabs.kvstore;

import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import java.util.Map;
import java.util.HashMap;

@ToString
@EqualsAndHashCode
public class KVStore implements Application {

  /* Usamos un hashmap para guardar en llave-valor a los registros
  que hagamos. */
  public Map<String, String> kvStore;

  public KVStore(){
    this.kvStore = new HashMap<>();
  }

  public interface KVStoreCommand extends Command {}

  /* Esta interfaz obliga a que los comandos tengan un metodo key()
  * para saber sobre cual llave van a a operar. */
  public interface SingleKeyCommand extends KVStoreCommand {
    String key();
  }

  @Data
  public static final class Get implements SingleKeyCommand {
    @NonNull private final String key;

    @Override
    public boolean readOnly() {
      return true;
    }
  }

  @Data
  public static final class Put implements SingleKeyCommand {
    @NonNull private final String key, value;
  }

  @Data
  public static final class Append implements SingleKeyCommand {
    @NonNull private final String key, value;
  }

  /* Interfaz para las respuestas que generemos con KVStores. */
  public interface KVStoreResult extends Result {}

  @Data
  public static final class GetResult implements KVStoreResult {
    @NonNull private final String value;
  }

  @Data
  public static final class KeyNotFound implements KVStoreResult {}

  @Data
  public static final class PutOk implements KVStoreResult {}

  @Data
  public static final class AppendResult implements KVStoreResult {
    @NonNull private final String value;
  }

  // Your code here...

  @Override
  public KVStoreResult execute(Command command) {
    /* Primero identificamos que tipo de comando es (get, put o append)
    * y asi poder hacer el cast correspondiente y poder acceder a sus
    * atributos. */

    /* Get: Es una peticion de lectura. No modifica el contenido en
     * la base de datos, solo lee y por eso tiene readOnly().
     *
     * Si es get: buscamos y guardamos su llave en la kvStores; vemos
     * que exista en el kvStore; si existe le creamos su valor en la
     * kvStores, en caso contrario regresa que no existe. */
    if (command instanceof Get) {
      Get g = (Get) command;
      // Your code here...
      String keyG = g.key();
      if(kvStore.containsKey(keyG)){
        String valueG = kvStore.get(keyG);
        return new GetResult(valueG);
      } else {
        return new KeyNotFound();
      }
    }

    /* Put: Es una peticion de escritura para asociar/ sobreescribir
    * una llave con un valor. Por eso pide el key y el value.
    *
    * Si es put: guardamos o reemplazamos la llave con su valor en el mapa
    * y regresamos un new PutOK() de que todo salio bien. */
    if (command instanceof Put) {
      Put p = (Put) command;
      // Your code here...
      String keyP = p.key();
      String valueP = p.value;
      kvStore.put(keyP, valueP);
      return new PutOk();
    }

    /* Append: Es una peticion de modificacion para agregar el nuevo texto
    * al final del valor que ya existia para una llave.
    *
    * Si es append: vemos si la llave ya existe, en caso de que si solo le
    * concatenamos el nuevo valor. En caso contrario lo tratamos como su fuera
    * el caso put, guardamos el resultado actualizado en el mapa y regresmos
    * un new AppendResult() y eso lo enviamos como valor final. */
    if (command instanceof Append) {
      Append a = (Append) command;
      // Your code here...
      String keyA = a.key();
      String valueA = a.value;
      String newValue = "";
      if(kvStore.containsKey(keyA)){
        String valorInicial = kvStore.get(keyA);
        newValue = valorInicial + valueA;
      } else {
        newValue = valueA;
      }
      kvStore.put(keyA, newValue);
      return new AppendResult(newValue);
    }

    /* En caso de que no sea ningun comando de los anteriores, lanzamos IAE. */
    throw new IllegalArgumentException();
  }
}

/* Esta clase es como una mini base de datos llave-valor en memoria.
 * Implementa la interfaz Aplication. Tiene los pasos siguientes:
 * recibe una instruccion de entrada (que es un objeto que
 * implementa Command), luego procesa la instruccion modificando
 * o consultando su estado interno (osea lo que es el hashmap
 * kvStore) y devuelve una respuesta (un objeto que implementa
 * a Result).*/
