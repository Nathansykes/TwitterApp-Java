package TwitterApp;

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.*;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class JSONConvert{

    public JSONObject StringToJSON(String JSONString) throws ParseException, org.json.simple.parser.ParseException
    {
        JSONObject obj;
        JSONParser parser = new JSONParser();
        String jsonString = JSONString;
        obj = (JSONObject)parser.parse(jsonString);
        return obj;
    }

    public JSONArray StringToJSONArray(String JSONString) throws ParseException, org.json.simple.parser.ParseException
    {
        JSONArray arr;
        JSONParser parser = new JSONParser();
        String jsonString = JSONString;
        arr = (JSONArray)parser.parse(jsonString);
        return arr;
    }

    public String JSONToString(JSONObject obj)
    {
        String JSONString = obj.toJSONString();



        return JSONString;
    }

}
