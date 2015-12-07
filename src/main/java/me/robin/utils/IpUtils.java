package me.robin.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2015/12/7.
 */
public class IpUtils {
    public static String getLocalIp() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (Exception e) {
            return "Unknown Host";
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        System.out.println(getLocalIp());
    }
}
