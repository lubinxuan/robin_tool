package me.robin.mail;

import java.io.Serializable;

/**
 * Created by Lubin.Xuan on 2015/9/2.
 * ie.
 */
public class SimpleMail implements Serializable{
    private String subject;
    private Object content;

    public SimpleMail(String subject, Object content) {
        this.subject = subject;
        this.content = content;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
