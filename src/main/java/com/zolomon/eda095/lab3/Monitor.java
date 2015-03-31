package com.zolomon.eda095.lab3;

public class Mailbox {
    private BlockingQueue<Message> messageQueue;
    private ArrayList< BlockingQueue<Message> > subscriberQueues;

    public Mailbox(BlockingQueue<Message> messageQueue) {
        this.messageQueue = messageQueue;
        this.subscriberQueues = new ArrayList<BlockingQueue<Message>>();
    }

    public synchronized void subscribe(BlockingQueue<Message> queue) {
        subscriberQueues.add(queue);
    }

    public synchronized void send(Message message) {
        messageQueue.put(message);
    }

    public synchronized void pump() {
        Message msg = messageQueue.take();
        for(BlockingQueue<Message> subscriber : subscriberQueues) {
            subscriber.put(msg);
        }
    }
}
