package com.dsoft.shetty.callblocker;

/**
 This class would hold details of the number that is blocked
 */

class BlockNumber {
    String pattern;
    String phoneNumber;

    public String getPattern()
    {
        return this.pattern;
    }

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public String getPhoneNumber()
    {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }
}
