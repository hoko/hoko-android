package com.hokolinks.model.exceptions;

public class InvalidDomainException extends HokoException {
    public InvalidDomainException(String domain) {
        super(15, domain + " is not a valid domain. It should be something like yourapp.hoko.link or your.customdomain.com");
    }
}
