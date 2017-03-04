
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
/*
This class is for selecting the result from the database. 

*/
public class MySQL {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_NAME = "phase1";
    private static final String URL = "jdbc:mysql://localhost/" + DB_NAME;
    private static final String DB_USER = "root";
    private static final String DB_PWD = "ccgroupyeah";

    private static Connection conn;

    private static String TEAMID = "tksaidwemustlovecc";
    private static String TEAM_AWS_ACCOUNT_ID = "863779383534";
    //this parameter stores the cache data. 
    static LinkedHashMap<String, String> cache = new LinkedHashMap<String, String>(5000);

    /**
     * Initializes database connection.
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void initializeConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);
    }
   
//Since we stores the byte array in database, we need to decode and get the original text. 
    public static String decode(String text) {
        //every byte is separted by " ". 
        String[] s = text.split(" ");
        //get the byte array size
        int size = s.length;
        byte[] textbyte = new byte[size];

        int size2 = 0;
        //translate the byte array string into byte array. 
        for (int i = 0; i < size; i++) {

            try {
                byte b = Byte.valueOf(s[i]);
                textbyte[i] = b;
                size2++;

            } catch (Exception e) {
                break;
            }
        }
        //since the database sometimes append multi " " and it cannot be decoded as byte, it should be ignored. get the new byte array. 
        Object array2 = Arrays.copyOf(textbyte, size2);

        byte[] text2 = (byte[]) array2;
        //translate from byte array to original text.
        String word = new String(text2);

        return word;

    }

    //transfer a string into a byte array string.

    public static String transferByteArrayString(String str) {
        byte[] sby = str.getBytes();
        StringBuilder strbuild = new StringBuilder();
        for (int i = 0; i < sby.length; i++) {

            strbuild.append(sby[i] + " ");
        }
        String byteString = strbuild.toString();
        return byteString;
    }
    //this method gets the result from cache or database and return the final response.
    public static String getResponse(String user_id, String hashtag) throws SQLException {
       
        String key = user_id + hashtag;
        //if the result is in the cache, return it directly
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        String tableName;
        //we split the whole dataset into 4 tables to reduce the parsing rows. 
        if (user_id.compareTo("182536580") < 0) {
            tableName = "Tweet1";
        } else if ((user_id.compareTo("182536580") >= 0) && (user_id.compareTo("2421570391") <= 0)) {
            tableName = "Tweet2";
        } else if ((user_id.compareTo("2421570432") >= 0) && (user_id.compareTo("438548299") <= 0)) {
            tableName = "Tweet3";
        } else {
            tableName = "Tweet4";
        }

        String response = "";
        Statement stmt = null;
        String hashtagbyte = transferByteArrayString(hashtag);
        //the first line of response
        String first = TEAMID + "," + TEAM_AWS_ACCOUNT_ID + "\n";
        StringBuilder str = new StringBuilder(first);
        try {
            stmt = conn.createStatement();
            
            //String sql = "select content from " + tableName + " where u_id='" + user_id + "' AND (hashtag like '%#" + hashtagbyte + "#%' or hashtag like '%#" + hashtagbyte + "' or hashtag='#" + hashtagbyte + "');";
            String sql = "select content from " + tableName + " where (hashtag like '%#" + hashtagbyte + "#%' or hashtag like '%#" + hashtagbyte + "' or hashtag='#" + hashtagbyte + "')  AND u_id = '" + user_id + "';";
            ResultSet rs = stmt.executeQuery(sql);
            //if no result return "\n";
            if (!rs.next()) {
                str.append("\n");
            } else {
                //get the response list.
                List<Response> resList = new LinkedList<Response>();
                do {
                    response = rs.getString("content");
                    //since the column may store many result include this user id and hashtag, they are sparted by ","
                    String[] multicontent = response.split(",");
                    for (int i = 0; i < multicontent.length; i++) {
                        //split every part of the content 
                        //part[0] is density, part[1] is time, part[2] is twitter id and part[3] is bytestring. 
                        String[] part = multicontent[i].split(":");
                        //decode the byte array string. 
                        String text = decode(part[3]);
                        
                        Response oneResponse = new Response(part[0], part[1], part[2], text);
                        resList.add(oneResponse);
                    }
                } while (rs.next());

                //sort the list.
                Collections.sort(resList);
                //append every result and generate the final result. 
                for (Response resp : resList) {
                    str.append(resp);
                }
               
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                 stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        

        str.append("\n");

        //store it in the cahce. 
        cache.put(key, str.toString());
        return str.toString();

    }

}
