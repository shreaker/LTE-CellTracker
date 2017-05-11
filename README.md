# LTE-CellTracker
Automatically parse LTE-Pcap-files (Wireshark) and show the position of the eNodeB (BaseStation) on a map. The project uses the API of opencellid.org, Google Geolocation and Google Maps.

# Mandatory dependencies
- Linux 64Bit
- Java 8 + JavaFX support (Oracle JDK recommended)
- libpcap-dev

# Use LTE-CellTracker
- Usage: CellTracker.jar -p -f -l
- p Parser used to get list of cells
- f File to PCAP files/folder
- l Locator used to get cell coordinates

- Provide your own API keys for opencellid.org and Google Geolocation in CellTracker.ini
