package me.robin.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Lubin.Xuan on 2015/10/8.
 * ie.
 */
public class AliveTestService {
    public boolean test(String host,int port){
        try {
            Socket socket = new Socket(host,port);
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            String ret = inputStream.readUTF();
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF("heart\r\n");
            ret = inputStream.readUTF();
            System.out.println(ret);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }




    public static void main(String[] args) {
        AliveTestService service = new AliveTestService();
        service.test("10.2.2.92",65432);
    }
}
