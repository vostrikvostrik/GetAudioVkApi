package com.vostrik.vk.audio.sevice;

/**
 * User: User
 * Date: 27.09.15
 * Time: 17:14
 */

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class VkMp3Download {


    public static void main(String[] args) throws URISyntaxException, IOException, org.json.simple.parser.ParseException {
        final String USER_ID = "4775676";
        final String ACCESS_TOKEN = "5SefQ7V9pXC3udGuoCDd";


        URIBuilder builder = new URIBuilder();
        builder.setScheme("https").setHost("api.vk.com").setPath("/method/audio.get")
                .setParameter("oid", USER_ID)
                .setParameter("need_user", "0")
                .setParameter("count", "20") // число загружаемых аудиозаписей
                .setParameter("offset", "20") // смещение, необходимое для выборки определенного количества аудиозаписей
                .setParameter("access_token", ACCESS_TOKEN);
        URI uri = builder.build();
        HttpGet httpget = new HttpGet(uri);

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream instream = null;
            try {
                instream = entity.getContent();
                String responseAsString = IOUtils.toString(instream);
                parseAndDownload(responseAsString);
            } finally {
                if (instream != null)
                    instream.close();
            }

        }



    }

    private static void parseAndDownload(String resp) throws IOException, org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(resp);
        JSONArray mp3list = (JSONArray) jsonResponse.get("response");
        for (int i=1; i<mp3list.size(); i++) {
            JSONObject mp3 = (JSONObject) mp3list.get(i);

            // папка должна существовать
            String pathname = "e:/music/" + fixWndowsFileName(mp3.get("artist") +
                    " - " + mp3.get("title"));
            try {
                File destination = new File(pathname + ".mp3");
                if (!destination.exists()) {
                    FileUtils.copyURLToFile(new URL((String) mp3.get("url")), destination);
                }
            } catch (FileNotFoundException e) {
                System.out.print("ERROR "+pathname);
            }
        }
    }

    private static String fixWndowsFileName(String pathname) {
        String[] forbiddenSymbols = new String[] {"<", ">", ":", "\"", "/", "\\", "|", "?", "*"};
        String result = pathname;
        for (String forbiddenSymbol: forbiddenSymbols) {
            result = StringUtils.replace(result, forbiddenSymbol, "");
        }
        return StringEscapeUtils.unescapeXml(result);
    }


}
