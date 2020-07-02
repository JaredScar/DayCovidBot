package com.jaredscarito.daycovidbot.main;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.file.YamlConfiguration;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {
    private static File infoFile = new File("info.yml");
    public static void main(String args[]) {
        if (!infoFile.exists()) {
            try {
                infoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Quotes random: https://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=json
        YamlConfiguration configY = YamlConfiguration.loadConfiguration(infoFile);
        ConfigurationBuilder cb = new ConfigurationBuilder();
        if (!configY.contains("Oauth")) {
            //Set defaults up
            configY.set("Oauth.ConsumerKey", "");
            configY.set("Oauth.ConsumerSecret", "");
            configY.set("Oauth.accesstoken", "");
            configY.set("Oauth.accesstokensecret", "");
            try {
                configY.save(infoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            cb.setDebugEnabled(true);
            cb.setOAuthConsumerKey(configY.getString("Oauth.ConsumerKey"));
            cb.setOAuthConsumerSecret(configY.getString("Oauth.ConsumerSecret"));
            cb.setOAuthAccessToken(configY.getString("Oauth.accesstoken"));
            cb.setOAuthAccessTokenSecret(configY.getString("Oauth.accesstokensecret"));
        }
        Configuration conf = cb.build();
        Twitter twitter = new TwitterFactory(conf).getInstance();
        //twitter.updateStatus("Testing DayCovid Bot");
        /**/
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // COVID Started on March 16th (for closings)
                    String quote = "";
                    String quoteAuthor = "";
                    int casesPositive = 0;
                    int deathCount = 0;
                    int recoveredCount = 0;
                    int deathIncrease = 0;
                    int hospitalized = 0;
                    int hopsitalizedIncrease = 0;
                    int positiveIncrease = 0;
                    long diff = 0;
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                    Date startDate = sdf.parse("03/12/2020");
                    Date today = new Date();
                    if (today.getHours() == 0 && today.getMinutes() == 0) {
                        long diffInMillies = Math.abs(today.getTime() - startDate.getTime());
                        diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                        URL urlForGetRequest = new URL("https://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=json");
                        String readLine = null;
                        HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
                        conection.setRequestMethod("GET");
                        //conection.setRequestProperty("method", "getQuote");
                        //conection.setRequestProperty("lang", "en");
                        //conection.setRequestProperty("format", "json");
                        conection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
                        int responseCode = conection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(conection.getInputStream()));
                            StringBuffer response = new StringBuffer();
                            while ((readLine = in.readLine()) != null) {
                                //System.out.println("The line is: " + readLine);
                                response.append(readLine);
                            }
                            in.close();
                            // print result
                            JsonObject jsonObject = new JsonParser().parse(response.toString()).getAsJsonObject();
                            //System.out.println("JSON String Result " + jsonObject.get("quoteText"));
                            quote = jsonObject.get("quoteText").toString().replaceAll(" \"", "").replaceAll("\"", "");
                            quoteAuthor = jsonObject.get("quoteAuthor").toString().replaceAll("\"", "");
                            //System.out.println("Quote is: " + quote + " - " + quoteAuthor);
                            //GetAndPost.POSTRequest(response.toString());
                        } else {
                            System.out.println("GET NOT WORKED");
                        }
                        urlForGetRequest = new URL("http://covidtracking.com/api/us");
                        readLine = null;
                        conection = (HttpURLConnection) urlForGetRequest.openConnection();
                        conection.setRequestMethod("GET");
                        //conection.setRequestProperty("method", "getQuote");
                        //conection.setRequestProperty("lang", "en");
                        //conection.setRequestProperty("format", "json");
                        conection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
                        responseCode = conection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(conection.getInputStream()));
                            StringBuffer response = new StringBuffer();
                            while ((readLine = in.readLine()) != null) {
                                //System.out.println("The line is: " + readLine);
                                response.append(readLine);
                            }
                            in.close();
                            JsonObject jsonObject = new JsonParser().parse(response.toString()).getAsJsonArray().get(0).getAsJsonObject();
                            casesPositive = jsonObject.get("positive").getAsInt();
                            deathCount = jsonObject.get("death").getAsInt();
                            recoveredCount = jsonObject.get("recovered").getAsInt();
                            deathIncrease = jsonObject.get("deathIncrease").getAsInt();
                            hospitalized = jsonObject.get("hospitalized").getAsInt();
                            hopsitalizedIncrease = jsonObject.get("hospitalizedIncrease").getAsInt();
                            positiveIncrease = jsonObject.get("positiveIncrease").getAsInt();
                            //GetAndPost.POSTRequest(response.toString());
                        } else {
                            System.out.println("GET NOT WORKED");
                        }
                        // We want to post it here
                        DecimalFormat formatter = new DecimalFormat("#,###");
                        twitter.updateStatus("It's day [" + diff + "] of Quarantine... Still quarantined...");
                        twitter.updateStatus("{COVID STATS} [Day " + diff + " of Quarantine]\n"
                                + "Increased Positive: " + formatter.format(positiveIncrease) + "\n"
                                + "Increased Hospitalizations: " + formatter.format(hopsitalizedIncrease) + "\n"
                                + "Increased Deaths: " + formatter.format(deathIncrease) + "\n"
                                + "Positive: " + formatter.format(casesPositive) + "\n"
                                + "Death Count: " + formatter.format(deathCount) + "\n"
                                + "Hospitalized: " + formatter.format(hospitalized) + "\n"
                                + "Recovered: " + formatter.format(recoveredCount) + "\n"
                                + "#covid19 #covid #covidstats #DayCovid"
                        );
                        twitter.updateProfile("DayCovid (Day " + diff + ")", "https://jaredscarito.com",
                                "Quarantine Start: 03/12/20",
                                "What day of Quarantine it is? I tweet once a day to keep you reminded. I also try to keep you sane with some inspirational quotes each day! (by @JaredScaritoo)");
                        if (quoteAuthor.length() > 0) {
                            twitter.updateStatus("{QUOTE} [Day " + diff + " of Quarantine]\n" +
                                    "\"" + quote + "\" - " + quoteAuthor);
                        } else {
                            // No author
                            twitter.updateStatus("{QUOTE} [Day " + diff + " of Quarantine]\n"
                                    + "\"" + quote + "\"");
                        }
                    } else {
                        // It is not the correct time
                        //System.out.println("[DEBUG] Can't post yet, it is not the correct time");
                    }
                } catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }, 0L, (1000L * 60)); // Every 60 seconds
        /**/

        // This is the status listener
        StatusListener tagListener = new StatusListener() {

            public void onStatus(Status status) {
                try {
                    // System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                    if(status.getUser().getId() !=twitter.getId()) {
                        if (!status.isRetweet()) {
                            // This is not a retweet
                            // They tweeted at us, check for a state
                            String[] tweetContents = status.getText().toLowerCase().split(" ");
                            String stateAbbrevSelected = null;
                            String stateNameSelected = null;
                            for (State state : State.values()) {
                                String stateAbbrev = state.getAbbreviation();
                                String stateName = state.getName();
                                for (String content : tweetContents) {
                                    if (content.equalsIgnoreCase(stateAbbrev)) {
                                        stateAbbrevSelected = stateAbbrev;
                                        stateNameSelected = stateName;
                                    } else
                                        if (status.getText().contains(stateName)) {
                                            stateAbbrevSelected = stateAbbrev;
                                            stateNameSelected = stateName;
                                        }
                                }
                            }
                            if (stateAbbrevSelected != null) {
                                // They have a valid state selected
                                try {
                                    URL urlForGetRequest = new URL("https://covidtracking.com/api/states");
                                    String readLine = null;
                                    HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
                                    conection.setRequestMethod("GET");
                                    //conection.setRequestProperty("method", "getQuote");
                                    //conection.setRequestProperty("lang", "en");
                                    //conection.setRequestProperty("format", "json");
                                    conection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
                                    int responseCode = conection.getResponseCode();
                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        BufferedReader in = new BufferedReader(
                                                new InputStreamReader(conection.getInputStream()));
                                        StringBuffer response = new StringBuffer();
                                        while ((readLine = in.readLine()) != null) {
                                            //System.out.println("The line is: " + readLine);
                                            response.append(readLine);
                                        }
                                        in.close();
                                        JsonArray arr = new JsonParser().parse(response.toString()).getAsJsonArray();
                                        JsonObject jsonObject = null;
                                        for (int i = 0; i < arr.size(); i++) {
                                            JsonObject obj = arr.get(i).getAsJsonObject();
                                            if (obj.get("state").getAsString().equalsIgnoreCase(stateAbbrevSelected)) {
                                                // It is the proper one
                                                jsonObject = obj;
                                                break;
                                            }
                                        }
                                        if (jsonObject != null) {
                                            twitter.createFavorite(status.getId());
                                            twitter.retweetStatus(status.getId());
                                            int casesPositive = 0;
                                            int deathCount = 0;
                                            int recoveredCount = 0;
                                            int deathIncrease = 0;
                                            int hospitalized = 0;
                                            int hopsitalizedIncrease = 0;
                                            int positiveIncrease = 0;
                                            if (jsonObject.has("positive") && !jsonObject.get("positive").isJsonNull()) {
                                                casesPositive = jsonObject.get("positive").getAsInt();
                                            }
                                            if (jsonObject.has("death") && !jsonObject.get("death").isJsonNull()) {
                                                deathCount = jsonObject.get("death").getAsInt();
                                            }
                                            if (jsonObject.has("recovered") && !jsonObject.get("recovered").isJsonNull()) {
                                                recoveredCount = jsonObject.get("recovered").getAsInt();
                                            }
                                            if (jsonObject.has("deathIncrease") && !jsonObject.get("deathIncrease").isJsonNull()) {
                                                deathIncrease = jsonObject.get("deathIncrease").getAsInt();
                                            }
                                            if (jsonObject.has("hospitalized") && !jsonObject.get("hospitalized").isJsonNull()) {
                                                hospitalized = jsonObject.get("hospitalized").getAsInt();
                                            }
                                            if (jsonObject.has("hospitalizedIncrease") && !jsonObject.get("hospitalizedIncrease").isJsonNull()) {
                                                hopsitalizedIncrease = jsonObject.get("hospitalizedIncrease").getAsInt();
                                            }
                                            if (jsonObject.has("positiveIncrease") && !jsonObject.get("positiveIncrease").isJsonNull()) {
                                                positiveIncrease = jsonObject.get("positiveIncrease").getAsInt();
                                            }
                                            DecimalFormat formatter = new DecimalFormat("#,###");
                                            StatusUpdate update = new StatusUpdate("@" + status.getUser().getScreenName() + "\n"
                                                    + "{COVID STATS} (" + stateAbbrevSelected + ") " + stateNameSelected + "\n\n"
                                                    + "Increased Positive: " + formatter.format(positiveIncrease) + "\n"
                                                    + "Increased Hospitalizations: " + formatter.format(hopsitalizedIncrease) + "\n"
                                                    + "Increased Deaths: " + formatter.format(deathIncrease) + "\n"
                                                    + "Positive: " + formatter.format(casesPositive) + "\n"
                                                    + "Death Count: " + formatter.format(deathCount) + "\n"
                                                    + "Hospitalized: " + formatter.format(hospitalized) + "\n"
                                                    + "Recovered: " + formatter.format(recoveredCount) + "\n"
                                                    + "#covid19 #covid #covidstats #DayCovid");
                                            update.inReplyToStatusId(status.getId());
                                            twitter.updateStatus(update);
                                        }
                                        //GetAndPost.POSTRequest(response.toString());
                                    } else {
                                        System.out.println("GET NOT WORKED");
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                // Not a valid state selected, we inform them they can get info from us if they give us a state
                                twitter.createFavorite(status.getId());
                                twitter.retweetStatus(status.getId());
                                StatusUpdate update = new StatusUpdate("@" + status.getUser().getScreenName() + "\n"
                                        + "You can get statistic information about COVID for a state by tagging me with the state name or abbreviation.\n\n"
                                        + "If you enjoy my services, make sure you follow @JaredScaritoo :)");
                                update.inReplyToStatusId(status.getId());
                                twitter.updateStatus(update);
                            }
                        }
                    }
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }
            public void onScrubGeo(long userId, long upToStatusId) {
                //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }
            @Override
            public void onStallWarning(StallWarning stallWarning) {
            }
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        FilterQuery fq = new FilterQuery();
        fq.track("@DayCovid", "DayCovid");
        TwitterStream twitterStream = new TwitterStreamFactory(conf).getInstance();
        twitterStream.addListener(tagListener);
        twitterStream.filter(fq);
    }
}
