package com.eventticketing.booking.client;

import feign.RetryableException;
import feign.Retryer;

public class BoundedFeignRetryer implements Retryer {
    private int maxAttempts;
    private int attempt;
    public BoundedFeignRetryer(){
        this(3);
    }

    private BoundedFeignRetryer(int maxAttempts){
        this.maxAttempts=maxAttempts;
    }

    @Override
    public Retryer clone() {
        return new BoundedFeignRetryer(maxAttempts);
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if(attempt>=maxAttempts-1) {
            System.out.println("All " + maxAttempts + " attempts exhausted");
            throw e;
        }
        attempt++;
        System.out.println(
                "Retrying: attempt " + (attempt + 1)
                        + " of " + maxAttempts
                        + " after: " + e.getMessage()
        );

        try {
            Thread.sleep(attempt==1?200:300);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }
}
