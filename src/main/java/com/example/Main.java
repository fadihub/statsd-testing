package com.example;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.metrics.StatService;
import io.advantageous.qbit.metrics.support.StatServiceBuilder;
import io.advantageous.qbit.metrics.support.StatsDReplicatorBuilder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceBundle;

import static io.advantageous.boon.core.IO.puts;

/**
 * Created by fadi on 6/18/15.
 */
public class Main {


    public static void main(String... args) {

        final StatReplicator statReplicator = StatsDReplicatorBuilder.statsDReplicatorBuilder()
                .setHost("192.168.59.105")
                .buildAndStart();


        final StatServiceBuilder statServiceBuilder = StatServiceBuilder.statServiceBuilder();
        statServiceBuilder.setReplicator(statReplicator);

        statServiceBuilder.getEndpointServerBuilder()
                .setPort(9999);

        statServiceBuilder.setServiceName("stat-service");


        final ServiceEndpointServer statServiceServer = statServiceBuilder.buildServiceServer();
        statServiceServer.start();

        ServiceEndpointServer server = statServiceServer;

        final ServiceBundle serviceBundle = server.serviceBundle();

        final StatService statService = serviceBundle.createLocalProxy(StatService.class, "stat-service");


        for (int index = 0; index < 100; index++) {
            statService.recordCount("xyz.count", index);
            statService.recordLevel("xyz.level", index);
            Sys.sleep(1000);

            serviceBundle.flushSends();
            statService.lastTenSecondCount(new Callback<Integer>() {
                @Override
                public void accept(Integer integer) {
                    puts("Ten seconds count for xyz.count", integer);
                }
            }, "xyz.count");

            statService.averageLastLevel(new Callback<Integer>() {
                @Override
                public void accept(Integer integer) {
                    puts("Average level  second count for xyz.level", integer);
                }
            }, "xyz.level", 10);

        }
    }




    }