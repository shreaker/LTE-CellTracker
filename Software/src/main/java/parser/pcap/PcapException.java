package parser.pcap;

public class PcapException extends Exception {

    public PcapException() {
    }

    public PcapException(String message) {
        super(message);
    }

    public PcapException(Throwable cause) {
        super(cause);
    }

    public PcapException(String message, Throwable cause) {
        super(message, cause);
    }
}
