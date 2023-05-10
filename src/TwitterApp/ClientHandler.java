package TwitterApp;


import java.io.*;
import java.net.*;
import java.nio.channels.ConnectionPendingException;
import java.nio.file.Files;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("unchecked")




public class ClientHandler extends Thread
{

    private Socket s;
    private PrintWriter pr;
    private BufferedReader in;
    //private String ClientId;

    public ClientHandler(Socket socket,String clientnumber) throws IOException
    {
        s = socket;
        //ClientId = clientnumber;
        pr = new PrintWriter(s.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }


    private JSONObject obj;
    private String recieved;
    private String SuccessString;
    private JSONObject GenericFailedObject = new JSONObject();


    JSONConvert Convert = new JSONConvert();

    private static Vector<Channel> channels = new Vector<>();
    private static Vector<Message> messages = new Vector<>();

    /*
        public ClientHandler(Vector channels, Vector messages)
        {
            this.channels = channels;
            this.messages = messages;
        }
    */
    @SuppressWarnings("unchecked ")
    public void run()
    {
//        if(channels.isEmpty()&&messages.isEmpty())//if a client handler has not yet run and put items in these vectors
//        {
//            try
//            {
//                //ReadChannelsFile();
//                //ReadSubscriptionsFile();
//                //ReadMessagesFile();
//            }
//            catch (Exception e)
//            {
//
//            }
//        }


        try
        {
            obj = new JSONObject();
            recieved = "";
            SuccessString = "{\"_class\":\"SuccessResponse\"}";

            GenericFailedObject.clear();
            GenericFailedObject.put("_class","ErrorResponse");
            GenericFailedObject.put("error","Failed");



            while((recieved= in.readLine()) != null)
            {
                String JSONString = DecodeMessage(recieved);
                obj = Convert.StringToJSON(JSONString);

                String classString = String.valueOf(obj.get("_class"));//get type of request
                String identity = String.valueOf(obj.get("identity"));
                //System.out.println(identity);


                switch(classString)//case to send each request to correct function
                {
                    case "OpenRequest":
                        OpenRequest(identity);
                        break;
                    case "SubscribeRequest":
                        String channel = String.valueOf(obj.get("channel"));
                        SubscribeRequest(identity, channel);
                        break;
                    case "PublishRequest":
                        //{"_class":"PublishRequest", "identity":"Alice", "message":{"_class":"Message","from":"Bob", "when":0, "body":"Hello again!"}}
                        JSONObject messageStringOBJ = (JSONObject)obj.get("message");

                        PublishRequest(messageStringOBJ);
                        break;
                    case "GetRequest":
                        //{"_class":"GetRequest", "identity":"Alice", "after":42}
                        String after = String.valueOf(obj.get("after"));

                        GetMessagesRequest(identity, after);
                        break;
                    case "UnsubscribeRequest":
                        String ChannelToUnsub = String.valueOf(obj.get("channel"));
                        UnsubscribeRequest(identity, ChannelToUnsub);
                        break;
                    case "GetChannelsRequest":
                        GetSubscribers(identity);
                        break;
                    default:

                        break;

                }
                //System.out.println("-------------------------------------------------END-------------------------------------------------");

            }


        }

        catch(Exception e)
        {
            System.out.println("Exception while connected");
            System.out.println(e.getMessage());

        }
    }

    public void ReadChannelsFile() throws IOException, ParseException,java.text.ParseException
    {
        System.out.println("Importing Channels from file");
        try
        {
            File file = new File("channels.json");
            Scanner FileReader = new Scanner(file);//reads in channels file
            while (FileReader.hasNextLine())
            {

                String line = FileReader.nextLine();
                obj = Convert.StringToJSON(line);

                String identity = String.valueOf(obj.get("identity"));//gets the channel name
                Channel channel = new Channel(identity);
                channels.add(channel);//create a channel
                System.out.println("Created Channel: "+identity);
            }
            FileReader.close();


        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found.");
            e.printStackTrace();
        }
    }

    public void ReadSubscriptionsFile() throws IOException, ParseException,java.text.ParseException
    {
        System.out.println("Importing Subscription data from file");
        try
        {
            File file = new File("subscriptions.json");
            Scanner FileReader = new Scanner(file);//reads in subs file
            int LineNumber  = 0;
            while (FileReader.hasNextLine())
            {
                LineNumber++;
                String line = FileReader.nextLine();
                //System.out.println(line);

                JSONObject obj = new JSONObject();
                try
                {
                    obj = Convert.StringToJSON(line);//puts json string into object

                } catch (Exception e)
                {
                    System.out.println("Message could not be read from file, Line: "+String.valueOf(LineNumber));
                    continue;//move onto next iteration if line cannot be read
                }

                String readIdentity = String.valueOf(obj.get("identity"));// gets the channel name
                JSONArray subsList = new JSONArray();
                subsList = (JSONArray)obj.get("channels");//gets the list of subscriptions

                Channel currentChannel = new Channel("tempchannel2");;


                for(int i = 0; i<channels.size();i++)//loops through channels
                {
                    if(channels.get(i).GetChannelName().equals(readIdentity));//if the channel is the channel that wants to be subscribing
                    {
                        currentChannel = channels.get(i);//set it as current channel
                    }

                }

                for(int x = 0; x<subsList.size();x++)//loop through the subsriptions
                {
                    for(int i = 0; i<channels.size();i++)//loops through channels
                    {
                        if(channels.get(i).GetChannelName().equals(subsList.get(x)));//if the channel equals the channel to sub to
                        {
                            currentChannel.subscribeToChannel(channels.get(i));//subsribe to it
                            System.out.println(currentChannel.GetChannelName()+" Subsribed to: "+ channels.get(i).GetChannelName());

                        }
                    }
                }




            }
            FileReader.close();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found.");
            e.printStackTrace();
        }
    }


    public void ReadMessagesFile() throws IOException, ParseException,java.text.ParseException
    {
        System.out.println("Importing Messages from file");
        try
        {
            File file = new File("messages.json");
            Scanner userFileReader = new Scanner(file);//reads in messages file
            while (userFileReader.hasNextLine())
            {
                String line = userFileReader.nextLine();
                //System.out.println(line);

                JSONObject obj = new JSONObject();
                obj = Convert.StringToJSON(line);

                String from = String.valueOf(obj.get("from"));
                String when = String.valueOf(obj.get("when"));
                String body = String.valueOf(obj.get("body"));
                String pic="";
                if(obj.containsKey("pic"))
                {
                    pic = String.valueOf(obj.get("pic"));
                }
                // gets all the date from json object into variables
                // gets pic if there is one


                Message message;
                if(obj.containsKey("pic"))
                {
                    message = new Message(body,from,when,pic);//creates the json message with a pic if there is one
                    messages.add(message);// add the message to the vector
                }
                else
                {
                    message = new Message(body,from,when);
                    messages.add(message);

                }
            }
            userFileReader.close();

        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found.");
            e.printStackTrace();
        }
    }





    public void OpenRequest(String identity) throws IOException
    {
        //-------------Open channel------------
        System.out.println("\n\n---------------OPEN REQUEST:---------------\n");
        boolean channelShouldOpen = true;
        if(!channels.isEmpty())
        {
            for(int i = 0;i<channels.size();i++)
            {
                if(channels.get(i).GetChannelName().equals(identity))//if channel with same name already exists
                {
                    channelShouldOpen = false;
                    break;
                }
                else
                {
                    channelShouldOpen = true;
                }
            }

        }
        else
        {
            channelShouldOpen = true;
        }

        if(channelShouldOpen)//if channel doesnt already exist
        {
            channels.add(new Channel(identity));// creates new channel and adds it to list

//            try(FileWriter fileWriter = new FileWriter("channels.json",true))
//            {
//                //{"_class":"channel", "identity":"NSD Client1"}
//                JSONObject channelOBJ = new JSONObject();
//                channelOBJ.put("_class", "channel");
//                channelOBJ.put("identity", identity);
//
//                fileWriter.write(Convert.JSONToString(channelOBJ)+"\n");
//            }//writes that channel to the file for persistence
//            catch(FileNotFoundException e)
//            {
//                System.out.println("file not found");
//            }

            System.out.println("Channel "+identity+" opened");
        }
        else
        {
            System.out.println("Channel "+identity+" already exists");
        }

        pr.println(EncodeMessage(SuccessString));



        System.out.println("Currently exisiting channels are: ");
        for(int i = 0; i < channels.size(); i++)
        {
            System.out.println(channels.get(i).GetChannelName());
        }
        System.out.println("\n---------------END OF OPEN REQUEST---------------\n\n");
        //-------------------------------------

    }






    public void SubscribeRequest(String identity, String channelName) throws IOException, ParseException, java.text.ParseException
    {
        //-------------Subscribe to channel------------
        System.out.println("\n\n---------------SUBSCRIBE REQUEST:---------------\n");
        Channel channelToSub = new Channel("tempChannel");
        Channel currentChannel = new Channel("tempChannel2");

        boolean channel1found = false, channel2found = false, alreadySubsribed = false;



        for(int i = 0; i<channels.size();i++)//loop through all channels
        {
            String tempChannelName = channels.get(i).GetChannelName();

            if(tempChannelName.equals(identity)&&!channel1found)//if channel name = current channel and channel has not already been set
            {
                currentChannel = channels.get(i);//copy it channel
                channel1found = true;
            }
            if(tempChannelName.equals(channelName)&&!channel2found)//if channel name = channel to subscribe to
            {
                channelToSub = channels.get(i);//copy it to channel
                channel2found = true;
            }
        }

        if(channel1found&&channel2found)
        {
            for (int i = 0; i < currentChannel.GetSubscribedChannels().size(); i++) // loops through subbed channels
            {
                if(currentChannel.GetSubscribedChannels().get(i).GetChannelName().equals(channelToSub.GetChannelName()))//if the channel to sub to is already subbed to
                {
                    alreadySubsribed = true;
                    break;
                }
                else
                {
                    alreadySubsribed = false;

                }
            }
            if (!alreadySubsribed)// if not subbbed to the channel
            {
                currentChannel.subscribeToChannel(channelToSub);// subscribe to the channel
                System.out.println("Subscribed To: "+channelToSub.GetChannelName());
                //writeSubsToFile();//write subscription data to file
                pr.println(EncodeMessage(SuccessString));
            }
            else
            {
                System.out.println("Already subscribed To: "+channelToSub.GetChannelName());
                String failMessage = CreateFailMessage("Already subscribed to: "+channelName);
                pr.println(EncodeMessage(failMessage));//already subbed error

            }

        }
        else//if the channel does not exist
        {
            String failMessage = CreateFailMessage("NO SUCH CHANNEL: "+channelName);
            pr.println(EncodeMessage(failMessage));
            System.out.println(failMessage);
        }

        System.out.println("Your Subscribed Channels are: ");
        for(int i = 0; i < currentChannel.GetSubscribedChannels().size(); i++)
        {
            System.out.println(currentChannel.GetSubscribedChannels().get(i).GetChannelName());
        }

        System.out.println("\n---------------END OF SUBSCRIBE REQUEST---------------\n\n");
        //---------------------------------------------
    }



    public void writeSubsToFile() throws IOException
    {

        String JSONString = "";


        for (int i = 0; i < channels.size(); i++) // loops through all channels
        {
            Vector<String> SubNames = new Vector<>();// vector to store names of channels subbed to
            Channel tempChannel = channels.get(i);// temp channel to use
            String identity = tempChannel.GetChannelName();// name of temp channel
            System.out.println("\n\n"+identity);

            if(!tempChannel.GetSubscribedChannels().isEmpty())//if not subscribed to any channels
            {
                int numOfChannels =tempChannel.GetSubscribedChannels().size();

                for (int x = 0; x < numOfChannels; x++)
                {
                    //System.out.println(tempChannel.GetSubscribedChannels().get(x).GetChannelName());
                    SubNames.add(tempChannel.GetSubscribedChannels().get(x).GetChannelName());
                }
            }


            //{"identity":"Alice", "channels":["Bob","jeff","gary"]}
            JSONObject msg = new JSONObject();
            JSONArray subsList = new JSONArray();

            for (int y = 0; y < SubNames.size(); y++)
            {
                subsList.add(SubNames.get(y));
            }
            msg.put("identity", identity);//name of channel
            msg.put("channels", subsList);//list of channels it is subbed to

            //System.out.println(Convert.JSONToString(msg));


            JSONString += Convert.JSONToString(msg)+"\n";//adds json to new line

        }
        try(FileWriter fileWriter = new FileWriter("subscriptions.json"))
        {
            fileWriter.write(JSONString);//writes all lines to file

        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }




    public void PublishRequest(JSONObject messageOBJ) throws IOException, ParseException, java.text.ParseException
    {
        //-------------Publish Messages------------

        System.out.println("\n\n---------------PUBLISH REQUEST:---------------\n");

        try
        {

            //{"_class":"Message","from":"Bob", "when":0, "body":"Hello again!"}
            //{"_class":"Message", "from":"Alice", "when":53, "body":"pic attached, base64 encoded,LOL", "pic":"iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAxlBMVEUAAADWAEX/"}

            String from = String.valueOf(messageOBJ.get("from"));

            String when ;

            if(messages.isEmpty())//if no messages have ever been sent
            {
                when = "1";//start at 1
            }
            else
            {
                int intWhen = messages.size();//get num of messages
                intWhen += 1;
                when = String.valueOf(intWhen);//set from as num of messages + 1
            }



            String body = String.valueOf(messageOBJ.get("body"));

            String pic = "";
            if(messageOBJ.containsKey("pic"))
            {
                pic = String.valueOf(messageOBJ.get("pic"));//get pic if it exists

            }
            if(body.length() > 1234)
            {
                String failedMessageString = CreateFailMessage("MESSAGE TOO BIG: 1234 characters");
                pr.println(EncodeMessage(failedMessageString));
                return;

            }

            Message message;
            if(messageOBJ.containsKey("pic"))
            {
                message = new Message(body,from,when,pic);//create message with pic
                messages.add(message);
                System.out.println("Message published:");
                System.out.println("Message: "+message.getBody());
                System.out.println("From: "+message.getFrom());
                System.out.println("When: "+message.getWhen());
                System.out.println("Pic: "+message.getPic());
            }
            else
            {
                message = new Message(body,from,when);//create message
                messages.add(message);
                System.out.println("Message published:");
                System.out.println("Message: "+message.getBody());
                System.out.println("From: "+message.getFrom());
                System.out.println("When: "+message.getWhen());
            }




//            try(FileWriter fileWriter = new FileWriter("messages.json",true))
//            {
//
//                JSONObject msg = new JSONObject();
//                msg.put("body",message.getBody());
//                msg.put("from",message.getFrom());
//                msg.put("when",message.getWhen());
//                if(messageOBJ.containsKey("pic"))
//                {
//                    msg.put("pic",message.getPic());
//                }
//                msg.put("_class", "message");
//
//                fileWriter.write(Convert.JSONToString(msg)+"\n");//write message to file
//            }

            pr.println(EncodeMessage(SuccessString));



        }
        catch(java.text.ParseException e)
        {
            String failedMessageString = CreateFailMessage("Failed Parse Exception");
            pr.println(EncodeMessage(failedMessageString));


        }
        catch (ParseException e)
        {
            String failedMessageString = CreateFailMessage("Failed Parse Exception");
            pr.println(EncodeMessage(failedMessageString));
        }

        System.out.println("\n---------------END OF PUBLISH REQUEST---------------\n\n");
        //-----------------------------------------
    }






    public void GetMessagesRequest(String identity, String after) throws IOException
    {
        //-------------Get Messages------------
        System.out.println("\n\n---------------GET MESSAGES REQUEST---------------\n");

        Vector<Channel> currentlySubscribedTo = new Vector<>();//temp vector for channels that current channel is subbed to
        Vector<String> MessagesToSend=new Vector<>();
        String FormattedMessagesToSend = "";

        for (int i = 0;i<channels.size();i++)//loop through all channels
        {
            if(channels.get(i).GetChannelName().equals(identity))//if channel name = current channel
            {
                currentlySubscribedTo = channels.get(i).GetSubscribedChannels();//adds the  channel to the vector if current channel is subbed to it
            }
        }



        for(int x = 0; x< currentlySubscribedTo.size(); x++)//loops through channels that current channel is subbed to
        {
            for(int i = 0; i<messages.size();i++)//loops through all messages
            {
                 System.out.println("message: \n"+messages.get(i).getBody());
                 System.out.println("messagefrom\n"+currentlySubscribedTo);
                if(messages.get(i).getFrom().equals(currentlySubscribedTo.get(x).GetChannelName()))//if the message is from a channel that current channel is subbed to
                {
                    if(Integer.parseInt(messages.get(i).getWhen()) > Integer.parseInt(after))//if the message is after specified time
                    {

                        //{"from":"ch2","_class":"message","body":"hello 3","when":"3"}

                        String from = messages.get(i).getFrom();
                        String When = messages.get(i).getWhen();
                        String body = messages.get(i).getBody();
                        String messageToSend = "{\"from\":\"" + from + "\",\"_class\":\"message\",\"body\":\"" + body + "\",\"when\":\"" + When + "\"";//create message json string
                        String pic = messages.get(i).getPic();

                        if(pic != null)
                        {
                            messageToSend += "\"pic\":\"" + pic + "\"";//add pic to json string if there is one

                        }
                        messageToSend += "}";//add ending bracket
                        MessagesToSend.add(messageToSend);
                        System.out.println("\n\n\nmessagetosend\n"+messageToSend+"\n\n\n\n");


                    }
                }
            }
        }

        FormattedMessagesToSend = "{\"_class\":\"MessageListResponse\", \"messages\":"+ MessagesToSend + "}";

        System.out.println(FormattedMessagesToSend);
        pr.println(FormattedMessagesToSend);

        System.out.println("\n---------------END GET MESSAGES REQUEST---------------\n\n");
        //-------------------------------------
    }





    public void GetSubscribers(String identity) throws IOException
    {
        //-------------Get channels------------
        System.out.println("\n\n---------------GET CHANNELS REQUEST---------------\n");

        Vector<Channel> currentlySubscribedTo = new Vector<>();//temp vector for channels that current channel is subbed to
        String FormattedMessageToSend = "";

        for (int i = 0;i<channels.size();i++)//loop through all channels
        {
            System.out.println("this channel: " + channels.get(i).GetChannelName());

            if(channels.get(i).GetChannelName().equals(identity))//if channel name = current channel
            {
                currentlySubscribedTo = channels.get(i).GetSubscribedChannels();//adds the  channel to the vector if current channel is subbed to it
            }
        }
        Vector<String> currentlySubscribedToChannelName = new Vector<>();
        System.out.println("Subbed to channels: " + currentlySubscribedTo.toString());

        for (int i = 0;i<currentlySubscribedTo.size();i++)//loop through Subbed channels
        {
            System.out.println("Subbed to channel names: "+currentlySubscribedTo.get(i).GetChannelName());
            currentlySubscribedToChannelName.add(currentlySubscribedTo.get(i).GetChannelName());//add the channel name to a list

        }

        //{"_class":"ChannelsListResponse", "channels":["channelName","channelName2"]}

        FormattedMessageToSend = "{\"_class\":\"ChannelsListResponse\", \"channels\":\""+ currentlySubscribedToChannelName.toString() + "\"}";

        System.out.println(currentlySubscribedToChannelName.toString());
        System.out.println(FormattedMessageToSend);
        pr.println(FormattedMessageToSend);

        System.out.println("\n---------------END GET CHANNELS REQUEST---------------\n\n");
        //-------------------------------------
    }








    public void UnsubscribeRequest(String identity, String channelName) throws IOException, ParseException,java.text.ParseException
    {


        //-------------Unsubscribe From Channel------------
        System.out.println("\n\n---------------UNSUBSCRIBE REQUEST---------------\n");
        Channel channelToUnsub = new Channel("tempChannel");
        Channel currentChannel = new Channel("tempChannel2");

        boolean channel1found = false, channel2found = false, Subsribed = false;



        for(int i = 0; i<channels.size();i++)//loop through all channels
        {
            String tempChannelName = channels.get(i).GetChannelName();

            if(tempChannelName.equals(identity)&&!channel1found)//if channel name = current channel and channel has not already been set
            {
                currentChannel = channels.get(i);//copy it channel
                channel1found = true;
            }
            if(tempChannelName.equals(channelName)&&!channel2found)//if channel name = channel to subscribe to
            {
                channelToUnsub = channels.get(i);//copy it to channel
                channel2found = true;
            }
        }

        if(channel1found&&channel2found)
        {
            for (int i = 0; i < currentChannel.GetSubscribedChannels().size(); i++)
            {
                if(currentChannel.GetSubscribedChannels().get(i).GetChannelName().equals(channelToUnsub.GetChannelName()))
                {
                    Subsribed = true;
                    break;
                }
                else
                {
                    Subsribed = false;

                }
            }
            if (Subsribed)
            {
                currentChannel.unsubscribeFromChannel(channelToUnsub.GetChannelName());;// unsubscribe to the channel
                System.out.println("Unsubscribed From: "+channelToUnsub.GetChannelName());
                //writeSubsToFile();
                pr.println(EncodeMessage(SuccessString));
            }
            else
            {
                System.out.println("Wasn't subscribed To: "+channelToUnsub.GetChannelName());
                String failMessage = CreateFailMessage("Wasn't subscribed to : "+channelName);
                pr.println(EncodeMessage(failMessage));

            }

        }
        else
        {
            String failMessage = CreateFailMessage("NO SUCH CHANNEL: "+channelName);
            pr.println(EncodeMessage(failMessage));
            System.out.println(failMessage);
        }

        System.out.println("Your Subscribed Channels are: ");
        for(int i = 0; i < currentChannel.GetSubscribedChannels().size(); i++)
        {
            System.out.println(currentChannel.GetSubscribedChannels().get(i).GetChannelName());
        }

        System.out.println("\n---------------END OF UNSUBSCRIBE REQUEST---------------\n\n");
        //-------------------------------------
    }






    public static String DecodeMessage(String recieved)
    {
        if(recieved == null)
        {
            recieved = "01";
        }
        byte[] encodedBytes = recieved.getBytes();

        Decoder decoder = Base64.getDecoder();

        byte[] decodedBytes = decoder.decode(encodedBytes);

        recieved = new String(decodedBytes);

        return recieved;
    }

    public static String EncodeMessage(String toSend)
    {
        byte[] decodedBytes = toSend.getBytes();

        Encoder encoder = Base64.getEncoder();

        byte[] encodedBytes = encoder.encode(decodedBytes);

        toSend = new String(encodedBytes);

        return toSend;
    }

    public String CreateFailMessage(String Reason) throws IOException, ParseException, java.text.ParseException
    {
        // takes reason and puts into json error message
        JSONObject failedMessage = new JSONObject();
        failedMessage.put("_class","ErrorResponse");
        failedMessage.put("error",Reason);

        String failedMessageString = String.valueOf(Convert.JSONToString(failedMessage));

        return failedMessageString;
    }




}
