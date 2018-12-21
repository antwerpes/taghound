package com.doccheck.twitter.taghound.example;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class ExampleStatusListener implements StatusListener {
        public void onStatus(Status status) {
            if (status.isRetweet()) {
                return;
            }
            System.out.println(status.getUser().getName() + " : " + status.getText() + "  Tweeted AT: " + status
                    .getCreatedAt());
        }

        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        }

        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        }

        public void onException(Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onScrubGeo(long arg0, long arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStallWarning(StallWarning arg0) {
            // TODO Auto-generated method stub

        }
}
