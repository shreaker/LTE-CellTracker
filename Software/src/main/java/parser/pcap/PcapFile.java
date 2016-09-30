package parser.pcap;

import org.jnetpcap.Pcap;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.util.PcapPacketArrayList;

public class PcapFile {

    private String file;

    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param file Name of the PCAP file.
     */
    public PcapFile(String file) {
        this.file = file;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Opens the offline Pcap-formatted file.
     *
     * @return PcapPacketArrayList  List of packets in the file
     * @throws PcapException Facing any erro in opening the file
     */
    public PcapPacketArrayList readOfflineFiles() throws PcapException {

        //First, setup error buffer and name for our file
        final StringBuilder errbuf = new StringBuilder(); // For any error msgs

        //Second ,open up the selected file using openOffline call
        Pcap pcap = Pcap.openOffline(file, errbuf);

        //Throw exception if it cannot open the file
        if (pcap == null) {
            throw new PcapException(errbuf.toString());
        }

        //Next, we create a packet handler which will receive packets from the libpcap loop.
        PcapPacketHandler<PcapPacketArrayList> jpacketHandler = (packet, paketsList) -> paketsList.add(packet);

        /***************************************************************************
         * (From jNetPcap comments:)
         * Fourth we enter the loop and tell it to capture unlimited packets. The loop
         * method does a mapping of pcap.datalink() DLT value to JProtocol ID, which
         * is needed by JScanner. The scanner scans the packet buffer and decodes
         * the headers. The mapping is done automatically, although a variation on
         * the loop method exists that allows the programmer to specify exactly
         * which protocol ID to use as the data link type for this pcap interface.
         **************************************************************************/

        try {

            PcapPacketArrayList packets = new PcapPacketArrayList();
            pcap.loop(-1, jpacketHandler, packets);

            return packets;

        } finally {
            pcap.close();
        }
    }
}
