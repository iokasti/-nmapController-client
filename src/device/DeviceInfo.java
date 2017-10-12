package device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

/**
 * The DeviceInfo class saves information about the device SA is running on.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 2.0
 * @since 2015-12-11
 */
public class DeviceInfo {
	/**
	 * Terminal device name.
	 */
	public static String deviceName;

	/**
	 * Interface IP.
	 */
	public static String ip;

	/**
	 * Interface MacAddr.
	 */
	public static String macAddress;

	/**
	 * Terminal OS Version.
	 */
	public static String osVersion;

	/**
	 * nmap Version.
	 */
	public static String nmapVersion;

	/**
	 * hash generated from the above.
	 */
	public static int hash;

	/**
	 * Constructor of DeviceInfo. Saves information about the device.
	 * 
	 * @throws IOException
	 */
	public static void getDeviceInfo() {
		try {
			/**** DEVICE NAME ****/
			deviceName = InetAddress.getLocalHost().getHostName();
			/*********************/

			/**** IP ADDRESS ****/
			InetAddress netAddrIp = null;
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface netIf : Collections.list(nets)) {
				if (!netIf.isUp() || netIf.isLoopback()) {
					// filters out inactive interfaces and loopback
					continue;
				}
				Enumeration<InetAddress> addresses = netIf.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr instanceof Inet4Address) {
						// filters out ipv6 addresses
						netAddrIp = addr;
						ip = addr.getHostAddress();
					}
				}
			}
			/********************/

			/**** MAC ADDRESS ****/
			NetworkInterface network = NetworkInterface.getByInetAddress(netAddrIp);
			byte[] mac = network.getHardwareAddress();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
			}
			macAddress = sb.toString();
			/********************/

			/**** OS VERSION ****/
			String command = "uname -mrs";
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			osVersion = "";
			String line;
			while ((line = input.readLine()) != null) {
				osVersion += line;
			}
			input.close();
			/********************/

			/**** NMAP VERSION ****/
			command = "nmap --version";
			p = Runtime.getRuntime().exec(command);
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String result = "";
			while ((line = input.readLine()) != null) {
				result += line;
				result += "\n";
			}
			input.close();
			nmapVersion = (String) result.subSequence(result.indexOf("version") + 8, result.lastIndexOf(" ("));
			/********************/

			/**** CREATE HASH ****/
			final int prime = 31;
			hash = 1;
			hash = prime * hash + ((deviceName == null) ? 0 : deviceName.hashCode());
			hash = prime * hash + hash;
			hash = prime * hash + ((ip == null) ? 0 : ip.hashCode());
			hash = prime * hash + ((macAddress == null) ? 0 : macAddress.hashCode());
			hash = prime * hash + ((nmapVersion == null) ? 0 : nmapVersion.hashCode());
			hash = prime * hash + ((osVersion == null) ? 0 : osVersion.hashCode());
			/********************/
		} catch (IOException e) {
			System.err.println("A problem occured while collecting device information.");
		}
	}

	/**
	 * @return Device info as json.
	 */
	@Override
	public String toString() {
		return "{\"deviceName\":\"" + deviceName + "\",\"ip\":\"" + ip + "\",\"macAddress\":\"" + macAddress
				+ "\",\"osVersion\":\"" + osVersion + "\",\"nmapVersion\":\"" + nmapVersion + "\",\"hash\":\"" + hash
				+ "\"}";
	}

}
