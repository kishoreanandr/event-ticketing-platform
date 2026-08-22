package com.eventticketing.event.service;

import com.eventticketing.event.dto.EventResponse;
import com.eventticketing.event.dto.EventUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EventServiceTestImpl {

    private final EventService eventService;

    public EventServiceTestImpl(EventService eventService){
        this.eventService=eventService;
    }

    public EventResponse updateEventTest(Long id, EventUpdateRequest request) throws InterruptedException {
        AtomicReference<EventResponse> response = new AtomicReference<>();

        CountDownLatch startLatch=new CountDownLatch(1);

        Thread t1=new Thread(()->{
            try{
                startLatch.await();
                System.out.println(Thread.currentThread().getName()+" is attempting to update");
                //response.set(eventService.updateEvent(id, request, organizerId));
                System.out.println(Thread.currentThread().getName()+" successfully updated");
            }catch (Exception ex){
                System.out.println(Thread.currentThread().getName()+" failed: "+ex.getMessage());
            }
        });
        Thread t2=new Thread(()->{
            try{
                startLatch.await();
                System.out.println(Thread.currentThread().getName()+" is attempting to update");
                //response.set(eventService.updateEvent(id, request, organizerId));
                System.out.println(Thread.currentThread().getName()+" successfully updated");
            }catch (Exception ex){
                System.out.println(Thread.currentThread().getName()+" failed: "+ex.getMessage());
            }
        });

        t1.start();
        t2.start();

        startLatch.countDown();

        t1.join();
        t2.join();

        return response.get();
    }
}
