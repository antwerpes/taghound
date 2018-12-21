package com.doccheck.twitter.taghound.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StreamHashTag {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamHashTag.class);

    public static void main(String[] args) {

        ExampleStatusListener listener = new ExampleStatusListener();

        Properties properties = loadProperties("conf/keys.properties");
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(properties.getProperty("consumer_key")).setOAuthConsumerSecret(properties.getProperty
                ("consumer_secret")).setOAuthAccessToken(properties.getProperty("oauth_token"))
                .setOAuthAccessTokenSecret(properties.getProperty("oauth_token_secret")).setTweetModeExtended(true);


        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(listener);

        FilterQuery filter = new FilterQuery();
        filter.track("#bonn");
        twitterStream.filter(filter);
    }

    /**
     * Load the properties file containing Twitter API credentials.
     *
     * @param filename the name of the file storing the keys.
     * @return a {@link Properties} object holding the credentials.
     */
    private static Properties loadProperties(String filename) {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(filename)) {
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("openBufferedStream failed!", e);
        }
        return properties;
    }
}
