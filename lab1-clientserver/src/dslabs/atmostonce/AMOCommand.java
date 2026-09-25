package dslabs.atmostonce;

import dslabs.framework.Command;
import lombok.Data;
import dslabs.framework.Address;

@Data
public final class AMOCommand implements Command {
  // Your code here...
  private final Command command;
  private final int sequenceNumber;
  private final Address clientAddress;
}
