package TwitterApp;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.*;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;



public class Message
{
    private String body;
    private String from;
    private String when;
    private String pic;

    public Message(String body, String from, String when)
    {
        this.body = body;
        this.from = from;
        this.when = when;
    }

    public Message(String body, String from, String when, String pic)
    {
        this.body = body;
        this.from = from;
        this.when = when;
        this.pic = pic;
    }

    public String getBody()
    {
        return body;
    }
    public String getFrom()
    {
        return from;
    }
    public String getWhen()
    {
        return when;
    }
    public String getPic()
    {
        return pic;
    }

}
