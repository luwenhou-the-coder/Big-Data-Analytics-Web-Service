/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.hbase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.filter.PrefixFilter;

/**
 *
 * @author yangxia
 */
public class HBase {

    private static String zkAddr = "172.31.13.191";
    /**
     * The name of your HBase table.
     */
    private static String tableName = "tweet";
    /**
     * HTable handler.
     */
    private static HTableInterface tweet;
    /**
     * HBase connection.
     */
    private static HConnection conn;
    /**
     * Byte representation of column family.
     */
    private static byte[] bColFamily = Bytes.toBytes("data");
    /**
     * Logger.
     */
    private final static Logger logger = Logger.getRootLogger();

    private static String TEAMID = "tksaidwemustlovecc";
    private static String TEAM_AWS_ACCOUNT_ID = "863779383534";
    public static ArrayList<String> multicontent = new ArrayList<String>();
    static LinkedHashMap<String, String> cache = new LinkedHashMap<String, String>(5000);

    /**
     * Initializes database connection.
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void initializeConnection() throws IOException {
        //initialization
        logger.setLevel(Level.ERROR);
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.master", zkAddr + ":60000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        conf.set("hbase.zookeeper.property.clientport", "2181");
        if (!zkAddr.matches("\\d+.\\d+.\\d+.\\d+")) {
            System.out.print("HBase not configured!");
            return;
        }
        conn = HConnectionManager.createConnection(conf);
        tweet = conn.getTable(Bytes.toBytes(tableName));
        System.out.println("Done, haha");
    }

    public static String decode(String text) {
        //parse a bytes array back to a string
        String[] s = text.split(" ");

        int size = s.length;
        byte[] textbyte = new byte[size];

        int size2 = 0;
        for (int i = 0; i < size; i++) {

            try {
                byte b = Byte.valueOf(s[i]);
                textbyte[i] = b;
                size2++;

            } catch (Exception e) {
                break;
            }
        }
        Object array2 = Arrays.copyOf(textbyte, size2);

        byte[] text2 = (byte[]) array2;
        String word = new String(text2);

        return word;

    }

    public static String transferByteArrayString(String str) {
        //parse a string to its bytes array
        byte[] sby = str.getBytes();
        StringBuilder strbuild = new StringBuilder();
        for (int i = 0; i < sby.length; i++) {

            strbuild.append(sby[i] + " ");
        }
        String byteString = strbuild.toString();
        return byteString;
    }

    public static String getResponse(String user_id, String hashtag) throws IOException {

        String key = user_id + hashtag;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        String tableName;

        byte[] prefix = Bytes.toBytes(user_id);
        Scan scan = new Scan();
        // prefix filter which is to filter the row_key
        PrefixFilter prefixFilter = new PrefixFilter(prefix);

        byte[] bColFamily = Bytes.toBytes("data");

        // Binary representation of the column name.
        byte[] content = Bytes.toBytes("content");
        byte[] bCol = Bytes.toBytes("hashtag");

        String response = "";
        String hashtagbyte = transferByteArrayString(hashtag);
        String first = TEAMID + "," + TEAM_AWS_ACCOUNT_ID + "\n";
        StringBuilder str = new StringBuilder(first);

        RegexStringComparator comp = new RegexStringComparator(hashtagbyte);
        /*
         This filter is to filter the hashtag
         */
        Filter filter = new SingleColumnValueFilter(bColFamily, bCol, CompareFilter.CompareOp.EQUAL, comp);

        FilterList filterList = new FilterList();
        /*
         add two in the filter in the filter list
         */
        filterList.addFilter(prefixFilter);
        filterList.addFilter(filter);

        try {
            scan.setFilter(filterList); //deploy the filter      
            scan.setBatch(10);

            ResultScanner rs = tweet.getScanner(scan);
            /*
             Get the content from Hbase, seperated by ","
             */
            for (Result r = rs.next(); r != null; r = rs.next()) {
                multicontent.add(Bytes.toString(r.getValue(bColFamily, content)));
            }
            /*
             append the new string to existed one
             */
            for (int i = 0; i < multicontent.size(); i++) {
                String singlecontent = multicontent.get(i);
                String[] part = singlecontent.split(",");
                Response oneResponse = new Response(part[0], part[1], part[2], decode(part[3]));
                str.append(oneResponse);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        str.append("\n");
        cache.put(key, str.toString());
        return str.toString();

    }

}
