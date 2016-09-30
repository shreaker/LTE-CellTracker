package parser;

import parser.pcap.PcapParser;

public class ParserFactory {

    public Parser getParser(String locatorType){

        Parser parser;

        switch (locatorType) {

            case "Pcap":
                parser = PcapParser.getInstance();
                break;
            default:
                parser = null;
        }

        return parser;
    }
}
