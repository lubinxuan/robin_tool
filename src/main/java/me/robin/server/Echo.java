package me.robin.server;

import me.robin.Const;

import java.io.*;
import java.net.Socket;

/**
 * Created by Lubin.Xuan on 2015/10/8.
 * ie.
 */
class Echo {

    private BufferedReader buf;

    private Socket acceptSocket;

    private DataOutputStream serverOut;

    public Echo(Socket acceptSocket) throws IOException {
        this.acceptSocket = acceptSocket;
        this.serverOut = new DataOutputStream(acceptSocket.getOutputStream());
        this.buf = new BufferedReader(new InputStreamReader(acceptSocket.getInputStream(), Const.UTF_8));
    }

    public void echo(Object msg) throws Exception {
        serverOut.writeUTF(String.valueOf(msg) + "\r\nc:");
        flush();
    }

    public void _echo(Object msg) throws Exception {
        serverOut.writeUTF(String.valueOf(msg));
        flush();
    }

    public String readLine() throws IOException {
        return buf.readLine();
    }

    private void flush() throws IOException {
        serverOut.flush();
    }

    public void close() throws Exception {
        buf.close();
        serverOut.close();
        acceptSocket.close();
    }
}