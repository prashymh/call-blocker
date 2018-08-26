package com.dsoft.shetty.callblocker;

/**
 * Reject call log details to be stored
 */

public class RejectLog {
    String phoneNumber;
    String dateTimeOfCall;

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public String getDateTimeOfCall()
    {
        return dateTimeOfCall;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public void setDateTimeOfCall(String dateTimeOfCall)
    {
        this.dateTimeOfCall = dateTimeOfCall;
    }
}
