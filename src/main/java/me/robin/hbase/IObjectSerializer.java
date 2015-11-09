package me.robin.hbase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by Lubin.Xuan on 2015/4/22.
 * ie.Iterator Object Serializer
 */
public class IObjectSerializer {

    private static final Logger logger = LoggerFactory.getLogger(IObjectSerializer.class);

    public static byte[] serialize(Object source) {
        if (null == source) {
            return null;
        }
        ByteArrayOutputStream byteStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteStream = new ByteArrayOutputStream(256);
            objectOutputStream = new ObjectOutputStream(byteStream);
            objectOutputStream.writeObject(source);
            objectOutputStream.flush();
            objectOutputStream.close();
            return byteStream.toByteArray();
        } catch (Exception e) {
            logger.error("序列化异常!!!", e);
            return null;
        } finally {
            if (null != objectOutputStream) {
                try {
                    objectOutputStream.close();
                } catch (IOException ignore) {
                }
            }

            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public static Object deserialize(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        ByteArrayInputStream byteStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteStream);
            return objectInputStream.readObject();
        } catch (Exception e) {
            logger.error("反序列化异常!!!", e);
            return null;
        } finally {
            if (null != objectInputStream) {
                try {
                    objectInputStream.close();
                } catch (IOException ignore) {
                }
            }

            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
