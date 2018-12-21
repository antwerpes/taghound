# taghound
Track popular twitter hashtags.

## The Java way
For the Java way, we use [twitter4j](http://twitter4j.org/en/index.html), an unofficial Java library for the Twitter API.

With twitter4j, you can stream tweets using a FilterQuery. You can apply different filters but note that Twitter filter parameters do not apply as AND but OR! You can filter either by `follow` (user id),`location` (geo coordinates), `language` or `track` (keyword). Each of these filters may receive a single value or a list of values.

* specify the users, by ID, to receive public tweets from.

    ```
    filter.follow(912665514996043777L); // this will track the handle @FlexikonDE
    ```
    
    If you don't know the ID of a user but only the handle, you can look it up [here](http://gettwitterid.com/).
    
* Filter by keywords

    ```
    filter.track("#importantHashTag"); 
    ```
    
* Filter by locations
    
    ```
    filter.locations(new double[]{-126.562500,30.448674}, new double[]{-61.171875,44.087585});
    ```
    
* Filter by language

    ```
    filter.language("en")
    ```
    
If you want to have several filters active, may be you can collect tweets on location and provide a filter logic of your own or collect keyword specific tweets and identify geographic distribution. Also, note that not all tweets have meta data like location or language set.

If, by chance, you have a Firehose, Links, Birddog and Shadow role (i.e. a higher access level than standard), you can also use the `count` parameter to set the number of previous statuses to stream before transitioning to the live stream. See also [here](https://stackoverflow.com/questions/6822121/parameter-count-not-allowed-in-twitter-filter-stream).
So, in general `filter.count(100)` will *not* work for you.
 
### Example
A basic example is [StreamHashTag](StreamHashTag) 

## The ELK-stack-way
This readme more or less contains a condensed copy of the original instruction that can be found [here](https://github.com/elastic/examples/tree/master/Common%20Data%20Formats/twitter).

### Installation & Setup
* Install the Elastic Stack

    If you do not have a working installation of the Elastic Stack, follow this [guide](https://github.com/elastic/examples/blob/master/Installation%20and%20Setup.md).

* Run Elasticsearch & Kibana

    ```
    <path_to_elasticsearch_root_dir>/bin/elasticsearch
    <path_to_kibana_root_dir>/bin/kibana
    ```
    
    On Windows, you _might_ need to say
    ```
    <path_to_elasticsearch_root_dir>/bin/elasticsearch.bat
    <path_to_kibana_root_dir>/bin/kibana.bat
    ```
        
* Check that Elasticsearch and Kibana are up and running.
   
    * Open `localhost:9200` in web browser -- should return status code 200
    * Open `localhost:5601` in web browser -- should display Kibana UI.

    By default, Elasticsearch runs on port 9200, and Kibana run on ports 5601. If you changed the default ports during installation, change the above calls to use appropriate ports.


* Download Example Files

    Download the following files in this repo to a local directory:
    - `twitter_logstash.conf` - Logstash configuration for ingesting data into Elasticsearch
    - `twitter_template.json` - template for custom mapping of fields. 
    - optionally: `twitter_kibana.json` - this is a configuration file for creating the Kibana dashboard

**Version**: This example has been tested in the following versions:
- Elasticsearch 6.1.1
- Logstash 6.1.1
- Kibana 6.1.1

### Run Example

#### 1. Configure example to use your Twitter API keys
* Get Twitter API keys and Access Tokens

    This example uses the Twitter API to monitor Twitter feed in real time. To use this, you will first need to [create a Twitter app](https://apps.twitter.com/app/new) to get your Twitter API keys and Access Tokens.

* Modify Logstash configuration file to use your Twitter API credentials

    Modify the `input { twitter { } }` section in the `twitter_logstash.conf` file to use the API keys and Access tokens generated   in the previous step. While at it, feel free to modify the words you want to track in the `keywords` field (in this example, we are tracking tweets mentioning popular Marvel Comic characters.
  ```
   input {
      twitter {
        # these are the credentials for your twitter app
        consumer_key       => "INSERT YOUR CONSUMER KEY"
        consumer_secret    => "INSERT YOUR CONSUMER SECRET"
        oauth_token        => "INSERT YOUR ACCESS TOKEN"
        oauth_token_secret => "INSERT YOUR ACCESS TOKEN SECRET"
        # select a number of keywords or hashtags
        keywords           => [ "thor", "spiderman", "wolverine", "ironman", "hulk"]
        # Record full tweet object as given to us by the Twitter Streaming API, default is false
        full_tweet         => true
        # ignore the retweets coming out of the Twitter API, default is false
        ignore_retweets   => true
      }
    }
  ```
    
* For a different use case, you will also want to modify the output configuration

    ```
     output {
       # this lets you see dots in the shell as new tweets are coming in
       stdout { codec => dots }
       # this is your elastic configuration with host, port, index, type and mapping template
       elasticsearch {
         hosts => "localhost:9200"
         index         => "twitter_elastic_example"
         document_type => "tweets"
         template      => "./twitter_template.json"
         # this is actually important: twitter objects are nested and this will help you resolve this
         template_name => "twitter_elastic_example"
         template_overwrite => true
       }
     }
   ```
   
   An example configuration (without credentials) is [here](conf/twitter.conf).
    
* More documentation on configuration can be found here:
    * [Configure the logstash twitter plugin input](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-twitter.html)
    * [Logstash configuration](https://www.elastic.co/guide/en/logstash/current/configuration.html)
    

#### 2. Ingest data into Elasticsearch using Logstash

* Execute the following command to start ingesting tweets of interest into Elasticsearch. Since this example is a monitoring Twitter in real time, the tweet ingestion volume will depend on the popularity of the words being tracked. When you run this command, you should see a trail of dots (`...`) in your shell as new tweets are ingested.

```shell
   <path_to_logstash_root_dir>/bin/logstash -f twitter_logstash.conf
```

On Windows, it _might_ be that you need to run 
```shell
   <path_to_logstash_root_dir>/bin/logstash.bat -f twitter_logstash.conf
```

* Verify that data is successfully indexed into Elasticsearch
    
    Running `http://localhost:9200/twitter_elastic_example/_count` should show a positive response for `count`.
    
    Running `http://localhost:9200/twitter_elastic_example/tweets/_search?q=*:*&pretty=true` will give you a preview of the indexed tweets.
    
    **Note:** Included `twitter_logstash.conf` configuration file assumes that you are running Elasticsearch on the same host as Logstash and have not changed the defaults. Modify the `host` and `cluster` settings in the `output { elasticsearch { ... } }` section of `twitter_logstash.conf`, if needed.


#### 3. Visualize data in Kibana

* Access Kibana by going to `http://localhost:5601` in a web browser
* Connect Kibana to the `twitter_elastic_example` index in Elasticsearch (autocreated in step 2)
    * Click the **Management** tab >> **Index Patterns** tab >> **Add New**. Specify `twitter_elastic_example` as the index pattern name and click **Create** to define the index pattern (Leave the **Use event times to create index names** box unchecked and the Event time as @timestamp)
    * If this is the only index pattern declared, you will also need to select the star in the top upper right to ensure a default is defined.
* Load sample dashboard into Kibana
    * Click the **Management** tab >> **Saved Objects** tab >> **Import**, and select `twitter_kibana.json`. On import you will be asked to overwrite existing objects - select "Yes, overwrite all". Additionally, select the index pattern "twitter_elastic_example" when asked to specify a index pattern for the dashboards.
* Open dashboard
    * Click on **Dashboard** tab and open `Sample Twitter Dashboard` dashboard. (Since we are visualizing twitter-feed in real time here, be sure to switch on the Auto-refresh option to see your dashboard update in real time)

### Notes & Hints
If you set the parameter `ignore_retweets=true`, the dashboard configuration will state an error. The influencers visualization relies on retweets, however this information is not fetched and thus neither indexed nor available in the mapping. 


# External documentation and further reading
* [General Twitter streaming documentation](https://developer.twitter.com/en/docs/tutorials/consuming-streaming-data)
* [Basic Stream Parameters](https://developer.twitter.com/en/docs/tweets/filter-realtime/guides/basic-stream-parameters)
* [Configure the tracking in the twitter stream](https://developer.twitter.com/en/docs/tutorials/consuming-streaming-data#track)
* [Filtering on locations](https://dev.twitter.com/docs/streaming-apis/parameters#locations).
* [Basic Twitter stream reading using Twitter4j](https://github.com/kantega/storm-twitter-workshop/wiki/Basic-Twitter-stream-reading-using-Twitter4j)