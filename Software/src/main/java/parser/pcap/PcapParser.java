package parser.pcap;

import items.Cell;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.util.PcapPacketArrayList;
import parser.Parser;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class PcapParser implements Parser {

    private static PcapParser instance;
    private static int DATA_OFFSET = 15; // data starts at byte 15

    ////////////////////////////////////////////////////////////////////////////

    private BitSet sib1BitSet;
    private int plmnOffset;

    ////////////////////////////////////////////////////////////////////////////

    private PcapParser() {
    }

    ////////////////////////////////////////////////////////////////////////////

    public static synchronized PcapParser getInstance() {

        if (PcapParser.instance == null) {
            PcapParser.instance = new PcapParser();
        }

        return PcapParser.instance;
    }

    /**
     * Parses a file or all files from a directory and adds found cells to the set.
     */
    public void getCells(String fileName, Set<Cell> cells) {

        File file = new File(fileName);
        List<String> fileNames = new ArrayList<>();

        if (!file.exists()) {
            return;
        }

        //get file names
        if (file.isFile()) {
            fileNames.add(file.getAbsolutePath());
        } else if (file.isDirectory()) {
            fileNames.addAll(Arrays.stream(file.listFiles()).filter(File::isFile).map(File::getAbsolutePath).collect(Collectors.toList()));
        } else {
            return;
        }

        fileNames.forEach((fn) -> {
            try {
                cells.addAll(parseFile(fn));
            } catch (PcapException e) {
                //todo do something here
            }
        });
    }

    /**
     * parse a single pcap file and return all found cells
     *
     * @param fileName
     * @return
     * @throws PcapException
     */
    private Set<Cell> parseFile(String fileName) throws PcapException {

        Set<Cell> cells = new HashSet<>();
        PcapFile pcap = new PcapFile(fileName);
        List<PcapPacket> sibs = findSIBs(pcap.readOfflineFiles());

        if (sibs.isEmpty()) {
            throw new PcapException();
        }

        cells.addAll(sibs.stream().map(this::dissectSIB1).collect(Collectors.toList()));

        return cells;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * parses a SIB1 packet for its MCC / MNC / TAC / CID
     * <p>
     * Some values have varying length and quantity, so the offset where
     * each value starts has to be calculated successively here. The plmnOffset-variable
     * keeps track of the current position in the bit-stream where we are currently operating.
     *
     * @param sib1
     * @return
     */
    private Cell dissectSIB1(PcapPacket sib1) {

        byte[] sib1Bytes = sib1.getByteArray(DATA_OFFSET, sib1.size() - DATA_OFFSET);
        this.sib1BitSet = fromByteArrayReverse(sib1Bytes);
        boolean hasMncThreeDigits;
        int numberOfPLMMNs, mcc = 0, mnc = 0, tac, cid, digit1, digit2, digit3;

        plmnOffset = 6;
        numberOfPLMMNs = getValueFromOffset(3) + 1; // there are x + 1 items in the PLMN-identity list
        plmnOffset = 8;

        for (int i = 0; i < numberOfPLMMNs; i++) {
            //get MCC -> 3 digits of 4 bits
            plmnOffset += 2;
            digit1 = getValueFromOffset(4) * 100;
            digit2 = getValueFromOffset(4) * 10;
            digit3 = getValueFromOffset(4);
            mcc = digit1 + digit2 + digit3;

            hasMncThreeDigits = getValueFromOffset(1) == 1;
            //get MNC -> 2-3 digits of 4 bits
            digit1 = getValueFromOffset(4);
            digit2 = getValueFromOffset(4);

            if (hasMncThreeDigits) {
                digit1 *= 100;
                digit2 *= 10;
                digit3 = getValueFromOffset(4);
                mnc = digit1 + digit2 + digit3;
            } else {
                mnc = digit1 * 10 + digit2;
            }
        }

        // we reached the end of the plmn list, tac and cid start here
        plmnOffset = plmnOffset + 1; // (not sure what this offset does)
        tac = getValueFromOffset(16); // get tac -> integer of 16 bits
        cid = getValueFromOffset(28); // get cid -> integer of 28 bits

        return new Cell(mcc, mnc, tac, cid);
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Reads a value with a specific size at the current position of plmnOffset
     *
     * @param size
     * @return
     */
    private int getValueFromOffset(int size) {

        int value = 0, end = this.plmnOffset + size;

        for (int i = 0; i < size; i++) {
            value += this.sib1BitSet.get(end - i - 1) ? (1 << i) : 0;
        }

        this.plmnOffset += size;
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Searches for SIB1 packets in a pcap-packet array
     *
     * @param packets
     * @return
     */
    private List<PcapPacket> findSIBs(PcapPacketArrayList packets) {
        //todo: bytes 0-15 are probably not a reliable marker for a sib1-packet, but it it's working for now
        return packets.stream().filter(packet -> (packet.getByte(0) == 0x01) &&
                (packet.getByte(1) == (byte) 0x01) && (packet.getByte(2) == (byte) 0x04) &&
                (packet.getByte(3) == (byte) 0x02) && (packet.getByte(4) == (byte) 0xff) &&
                (packet.getByte(5) == (byte) 0xff) && (packet.getByte(6) == (byte) 0x03) &&
                (packet.getByte(7) == (byte) 0x00) && (packet.getByte(8) == (byte) 0x00) &&
                (packet.getByte(9) == (byte) 0x04) && (packet.getByte(10) == (byte) 0x00) &&
                (packet.getByte(11) == (byte) 0x05) && (packet.getByte(12) == (byte) 0x07) &&
                (packet.getByte(13) == (byte) 0x01) && (packet.getByte(14) == (byte) 0x01))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Puts the bits in a byte array in reversed order
     *
     * @param bytes
     * @return
     */
    private BitSet fromByteArrayReverse(final byte[] bytes) {

        final BitSet bits = new BitSet();

        for (int i = 0; i < bytes.length * 8; i++) {

            if ((bytes[i / 8] & (1 << (7 - (i % 8)))) != 0) {
                bits.set(i);
            }
        }

        return bits;
    }
}
