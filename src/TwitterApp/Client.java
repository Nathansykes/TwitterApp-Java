package TwitterApp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("unchecked")
public class Client {

    private static String identity = "";
    private static boolean ShouldRun = true;
    private static JSONConvert Convert = new JSONConvert();
    final private static String hostName = "localhost";
    final private static int portNumber = 12345;
    private static Socket Socket;
    private static PrintWriter out;
    private static Scanner in;
    private static BufferedReader stdIn;

    //public static void main(String[] args) throws IOException
    public static void run() throws IOException
    {


        Socket = new Socket(hostName, portNumber); //create server link
        out = new PrintWriter(Socket.getOutputStream(), true); //to print to server
        in = new Scanner(Socket.getInputStream()); //receive from server

        //stdIn =new BufferedReader(new InputStreamReader(System.in));//to read keyboard no longer needed


    }

    public static boolean executeCommand(String userInput) throws Exception
    {
        boolean Success = false;
        boolean isGetMessageRequest = false;

        char requestType = userInput.charAt(0);
        char checkSpace = userInput.charAt(1);
        requestType = Character.toLowerCase(requestType);
        userInput = userInput.substring(2);

        String outputMessage = "";


        switch (requestType)
        {
            case 'o'://open request
                //    "o channelName"
                identity = userInput;
                outputMessage = getOpenRequest(userInput);
                System.out.println("Opening channel "+userInput);
                break;
            case 's'://subscribe request
                //   "s channelName"
                outputMessage = getSubscribeRequest(identity, userInput);
                System.out.println("Subscribing to "+userInput);
                break;
            case 'p'://publish request
                String body = userInput;
                String pic = "";
                try
                {
                    pic = userInput.substring(userInput.lastIndexOf("pic attached")+13);
                    body = userInput.substring(0,userInput.indexOf("pic attached"));
                    System.out.println(body);
                    System.out.println(pic);
                }
                catch (Exception ignored)
                {

                }
                outputMessage = getPublishRequest(identity ,body, 0,pic);
                System.out.println("Publishing message "+userInput);
                break;
            case 'g'://get message request
                outputMessage = getMessageRequest(identity, Integer.parseInt(userInput));
                //isGetMessageRequest = true;
                //System.out.println("Getting messages");
                break;
            case 'u'://unsubscribe request
                outputMessage = getUnsubscribeRequest(identity, userInput);
                System.out.println("Unsubscribing from "+userInput);
                break;
            case 'c'://get channels request
                outputMessage  = getChannelsRequest(identity);
                System.out.println("Getting subscribed channels of"+identity);
            default:


                break;
        }




        out.println(outputMessage);
        String response = "";

        response = readResult(in);
        System.out.println("Response is : " + response);
        if(response == "Success")
        {
            Success = true;
        }


        return Success;
    }



    public static Vector<String> getChannels(String channel) throws Exception
    {
        //{"_class":"ChannelsListResponse", "channels":["channelName","channelName2"]}

        //{\"_class\":\"ChannelsListResponse\", \"channels\":["channelName","channelName"]}

        String outputMessage= getChannelsRequest(channel);
        out.println(outputMessage);
        String result = in.nextLine();
        System.out.println(result);

        JSONObject responseObject = new JSONObject();
        String[] channelsArray;
        responseObject = Convert.StringToJSON(result);

        String ArrayString = String.valueOf(responseObject.get("channels"));

        ArrayString = ArrayString.replace("[", "");
        ArrayString = ArrayString.replace("]", "");
        System.out.println(ArrayString);
        channelsArray = ArrayString.split(", ");

        Vector<String> channelsList = new Vector<>();

        for(int i = 0; i<channelsArray.length;i++)
        {
            String channelName = channelsArray[i].toString();

            channelsList.add(channelName);
        }



        return channelsList;
    }
    private static String getChannelsRequest(String channel)
    {


        // create request Object
        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.GETCHANNELSCLASSNAME);
        obj.put("identity", channel);



        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }

    public static Vector<Message> getMessages() throws Exception
    {
        //result should equal:
        //{\"_class\":\"MessageListResponse\", \"messages\":["{"from":"ch2","_class":"message","body":"hello 1","when":"3"}","{"from":"ch2","_class":"message","body":"hello 2","when":"3"}"]}

        String outputMessage= getMessageRequest(identity,0);

        out.println(outputMessage);
        String result = in.nextLine();
        System.out.println(result);


        JSONObject responseObject = new JSONObject();
        JSONArray messagesArray = new JSONArray();
        responseObject = Convert.StringToJSON(result);

        String JSONArrayString = String.valueOf(responseObject.get("messages"));
        messagesArray = Convert.StringToJSONArray(JSONArrayString);

        Vector<Message> messageList = new Vector<>();
        String messages = "";
        for(int i = 0; i<messagesArray.size();i++)
        {
            JSONObject obj = new JSONObject();
            obj = (JSONObject)messagesArray.get(i);
            String when = String.valueOf(obj.get("when"));
            String from = String.valueOf(obj.get("from"));
            String body = String.valueOf(obj.get("body"));
            String pic = "";
            if(obj.containsKey("pic"))
            {
                pic = String.valueOf(obj.get("pic"));

            }
            if(pic == "")
            {
                messageList.add(new Message(body,from,when));
            }
            else
            {
                messageList.add(new Message(body,from,when,pic));
            }
        }


        return messageList;
    }

    //Read the result from a request. Does not do anything if the result is ok else throws exception leading to system exit
    private static String readResult(Scanner in) throws Exception
    {
        String result = in.nextLine();

        byte[] encodedBytes = result.getBytes();

        Decoder decoder = Base64.getDecoder();

        byte[] decodedBytes = decoder.decode(encodedBytes);

        result = new String(decodedBytes);

        System.out.println();

        String responseString = "";
        JSONObject responseOBJ = Convert.StringToJSON(result);
        String responseClass = String.valueOf(responseOBJ.get("_class"));
        if(responseClass.equals("SuccessResponse"))
        {
            responseString= "Success";
        }
        else if (responseClass.equals("ErrorResponse"))
        {
            responseString = "Failed: "+String.valueOf(responseOBJ.get("error"));
        }

        return responseString;

    }

    //This is the format. {"_class":"OpenRequest", "identity":"Alice"}.
    @SuppressWarnings("unchecked")
    private static String getOpenRequest(String channel)
    {


        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.OPENCLASSNAME);
        obj.put("identity", channel);


        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }

    //This is the format. {"_class":"SubscribeRequest", "identity":"Alice", "channel":"Bob"}.
    @SuppressWarnings("unchecked")
    private static String getSubscribeRequest(String user, String channel)
    {

        //System.out.println("\n Json String");

        // create request Object
        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.SUBSCRIBECLASSNAME);
        obj.put("identity", user);
        obj.put("channel", channel);

        //return obj.toJSONString();
        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }

    //This is the format. {"_class":"PublishRequest", "identity":"Alice", "message":{"_class":"Message", "from":"Bob", "when":0, "body":"Hello again!"}}.
    @SuppressWarnings("unchecked")
    private static String getPublishRequest(String sender, String message, int time, String pic)
    {
        // create message Object first
        JSONObject msg = new JSONObject();
        msg.put("_class",Parameters.MESSAGECLASSNAME);
        msg.put("from", sender);
        msg.put("when", time);
        msg.put("body", message);
        if(pic != "")
        {
            msg.put("pic",pic);
        }


        String messageString = msg.toJSONString();

        // create request Object
        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.REQUESTCLASSNAME);
        obj.put("identity", sender);
        obj.put("message", msg);


        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }

    //This is the format.{"_class":"UnsubscribeRequest", "identity":"Alice", "channel":"Bob"}
    @SuppressWarnings("unchecked")
    private static String getUnsubscribeRequest(String user, String channel)
    {


        // create request Object
        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.UNSUBSCRIBECLASSNAME);
        obj.put("identity", user);
        obj.put("channel", channel);


        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }

    //This is the format {"_class":"GetRequest", "identity":"Alice", "after":42}
    @SuppressWarnings("unchecked")
    private static String getMessageRequest(String channel,int time)
    {


        // create request Object
        JSONObject obj = new JSONObject();
        obj.put("_class",Parameters.GETCLASSNAME);
        obj.put("identity", channel);
        obj.put("after", time);



        byte[] encodedBytes =  Base64.getEncoder().encode(obj.toJSONString().getBytes());
        String encodedText = new String(encodedBytes);
        return encodedText;
    }

}

