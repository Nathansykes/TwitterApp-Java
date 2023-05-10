package TwitterApp;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.*;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;


public class Channel
{
    private String ChannelName = "";
    private Vector<Channel> SubscribedTo = new Vector<>();

    public Channel(String ChannelName)
    {
        this.ChannelName = ChannelName;
    }

    public void subscribeToChannel(Channel channelToSub)
    {
        SubscribedTo.add(channelToSub);
    }

    public void unsubscribeFromChannel(String channelToUnsub)
    {
        for(int i = 0; i<SubscribedTo.size();i++)
        {
            if(SubscribedTo.get(i).GetChannelName().equals(channelToUnsub))
            {
                SubscribedTo.remove(i);
            }
        }
    }

    public String GetChannelName()
    {
        return ChannelName;
    }

    public Vector<Channel> GetSubscribedChannels()
    {
        return SubscribedTo;
    }

}
