import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

/**
 * Created by Lubin.Xuan on 2016/1/26.
 */
public class SocketHttpDemo {

    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(30000);
        socket.setKeepAlive(true);
        socket.setReuseAddress(true);
        socket.setReceiveBufferSize(4096);
        socket.setSendBufferSize(4096);


        String urls[] = new String[]{
                "https://item.taobao.com/item.htm?id=520028969788&ns=1&abbucket=2",
                "https://item.taobao.com/item.htm?id=520029108956&ns=1&abbucket=6",
                "https://item.taobao.com/item.htm?id=520029220723&ns=1&abbucket=2",
                "https://item.taobao.com/item.htm?id=520029337092&ns=1&abbucket=6",
                "https://item.taobao.com/item.htm?id=520029347690&ns=1&abbucket=6"
        };

        InetSocketAddress sockAddr = new InetSocketAddress("item.taobao.com", 443);
        socket.connect(sockAddr, 30000);

        socket = createSSLSocket(socket, sockAddr.getHostString(), sockAddr.getPort());

        for (String url : urls) {
            sendHttp(socket, url);
        }

        socket.close();
    }


    private static Socket createSSLSocket(Socket socket, String host, int port) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) factory.createSocket(socket, host, port, true);
        sslsocket.setUseClientMode(true);
        sslsocket.setEnabledProtocols(sslsocket.getSupportedProtocols());
        sslsocket.setEnabledCipherSuites(sslsocket.getSupportedCipherSuites());
        sslsocket.startHandshake();
        return sslsocket;
    }

    private static void sendHttp(Socket socket, String url) throws IOException {
        URL u = new URL(url);
        socket.getOutputStream().write(reqBody(u).getBytes("utf-8"));
        socket.getOutputStream().flush();
        System.out.println("------------------------------------------------------------------------");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("resp:" + url);
        printRsp(socket.getInputStream());
    }


    private static String reqBody(URL url) {

        String path = "".equals(url.getFile()) ? "/" : url.getFile();

        // some servers will redirect a request with a host line like
        // "Host: <hostname>:80" to "http://<hpstname>/<orig_path>"- they
        // don't want the :80...

        String host = url.getHost();
        int port;
        String portString;
        if (url.getPort() == -1) {
            portString = "";
        } else {
            port = url.getPort();
            portString = ":" + port;
        }


        StringBuilder reqStr = new StringBuilder("GET ");
        reqStr.append(path);

        reqStr.append(" HTTP/1.0\r\n");

        reqStr.append("Host: ");
        reqStr.append(host);
        reqStr.append(portString);
        reqStr.append("\r\n");

        reqStr.append("Accept-Encoding: x-gzip, gzip\r\n");
        reqStr.append("Connection: Keep-Alive\r\n");

        reqStr.append("Accept: ");
        reqStr.append("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        reqStr.append("\r\n");

        reqStr.append("User-Agent: ");
        reqStr.append("rot_zju_si");
        reqStr.append("\r\n");
        reqStr.append("\r\n");

        return reqStr.toString();
    }


    private static void printRsp(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] tmp = new byte[4096];
        while (-1 != is.read(tmp)) {
            bos.write(tmp);
        }
        System.out.println(new String(bos.toByteArray(), "gb2312"));
    }


    private static String[] protocols = new String[]{"TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3"};
    private static String[] ciphers = new String[]{
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
            "SSL_RSA_WITH_RC4_128_SHA", "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
            "TLS_ECDH_RSA_WITH_RC4_128_SHA",
            "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
            "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
            "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
            "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
            "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_RC4_128_MD5",
            "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", "TLS_RSA_WITH_NULL_SHA256",
            "TLS_ECDHE_ECDSA_WITH_NULL_SHA", "TLS_ECDHE_RSA_WITH_NULL_SHA",
            "SSL_RSA_WITH_NULL_SHA", "TLS_ECDH_ECDSA_WITH_NULL_SHA",
            "TLS_ECDH_RSA_WITH_NULL_SHA", "SSL_RSA_WITH_NULL_MD5",
            "SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA",
            "SSL_DHE_DSS_WITH_DES_CBC_SHA", "TLS_KRB5_WITH_RC4_128_SHA",
            "TLS_KRB5_WITH_RC4_128_MD5", "TLS_KRB5_WITH_3DES_EDE_CBC_SHA",
            "TLS_KRB5_WITH_3DES_EDE_CBC_MD5", "TLS_KRB5_WITH_DES_CBC_SHA",
            "TLS_KRB5_WITH_DES_CBC_MD5"};
}
