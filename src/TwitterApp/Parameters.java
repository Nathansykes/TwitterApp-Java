package TwitterApp;


//This is to store all parameters to be used as constant
public class Parameters
{

    public static String SERVERHOST = null;
    public static int SERVERPORT = 0;
    public static boolean DEBUG = false;
    public static String LOAD = null;

    public static String LOW = "Low";
    public static String HIGH = "High";
    public static String REGULAR = "Routine";

    public static String OPENCLASSNAME = "OpenRequest";
    public static String REQUESTCLASSNAME = "PublishRequest";
    public static String SUBSCRIBECLASSNAME = "SubscribeRequest";
    public static String UNSUBSCRIBECLASSNAME = "UnsubscribeRequest";
    public static String GETCLASSNAME = "GetRequest";
    public static String GETCHANNELSCLASSNAME = "GetChannelsRequest";
    public static String MESSAGECLASSNAME = "Message";

    public static String SUCCESSCLASSNAME = "SuccessResponse";
    public static String ERRORCLASSNAME = "ErrorResponse";
    public static String ERRORNAME = "error";



    public static void printAll()
    {
        if (!DEBUG) System.out.println("I will not print as you do not want me to ");

        else
        {
            System.out.println("Server is at "+ SERVERHOST + " listening to "+ SERVERPORT);
            System.out.println(" Throughput is "+ LOAD);
            System.out.println( " I will show you the proceedigns ");
        }
    }

    public static void debug(String message)
    {
        if (DEBUG) System.out.println(message);
    }
}