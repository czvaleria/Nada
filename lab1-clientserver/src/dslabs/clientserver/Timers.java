package dslabs.clientserver;

import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Timer;
import lombok.Data;

@Data
final class ClientTimer implements Timer {
  static final int CLIENT_RETRY_MILLIS = 100;

  // Your code here...
  private final int sequenceNumber;

    public ClientTimer(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
