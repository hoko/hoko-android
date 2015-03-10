package com.hoko.model.exceptions;

/**
 * Created by ivanbruel on 21/01/15.
 */
public class HokoGooglePlayServicesException extends HokoException {
    public HokoGooglePlayServicesException() {
        super(8, "There seems to be an issue with your Google Cloud Messaging key " +
                "or the signing of your application");
    }
}