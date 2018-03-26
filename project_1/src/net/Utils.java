package net;

import misc.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Utils {
    public static String getLocalIP() throws IOException {
        URL connection = null;
        try {
            connection = new URL("http://checkip.amazonaws.com/");
            URLConnection con = connection.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            return reader.readLine();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String removeUTF8BOM(String s) {
        if (s.startsWith(Constants.UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }
}
