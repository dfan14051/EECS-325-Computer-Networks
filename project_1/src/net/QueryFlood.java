package net;

import misc.Constants;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.*;

public class QueryFlood {

    public static String fetchFileLocation(String fileName, Set<Socket> neighbors, int query){
        ExecutorService executor = Executors.newFixedThreadPool(2);
        LinkedList<Future<String>> futures = new LinkedList<>();
        for (Socket neighbor : neighbors) {
            futures.add(executor.submit(new QueryFloodTask(neighbor, fileName, query)));
        }
        executor.shutdown();

        String response = null;
        String temp;
        for (Future<String> future : futures) {
            try {
                temp = future.get(Constants.timeout, TimeUnit.MILLISECONDS);
                if(temp != null){
                    response = temp;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                future.cancel(true);
            }
        }

        return response;
    }
}
