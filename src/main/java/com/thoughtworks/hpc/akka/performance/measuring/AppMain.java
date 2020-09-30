package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.ActorSystem;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class AppMain {
    public static void main(String[] cliArgs) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        boolean scriptMode = false;

        for (String cliArg : cliArgs) {
            if (cliArg.equals("--scriptMode")) {
                scriptMode = true;
                break;
            }
        }

        ActorSystem<RootActor.Command> system = ActorSystem.create(RootActor.create(), "akka-performance-measuring");
        int n;
        CountDownLatch finish;
        int parallelism;

        while (true) {
            if (!scriptMode) {
                System.out.print("> ");
            }
            String line = scanner.nextLine().trim();

            String[] args = line.split(" ");
            if (args.length == 0) {
                continue;
            }
            String command = args[0].toLowerCase();
            switch (command) {
                case "h":
                case "help":
                    // Todo: 输出帮助信息
                    break;
                case "q":
                case "quit":
                    System.exit(0);
                    break;
                case "n":
                    System.out.println("hello world");
                    break;
                case "enqueueing":
                    // enqueueing n
                    n = Integer.parseInt(args[1]);

                    finish = new CountDownLatch(1);
                    system.tell(new RootActor.HandleEnqueueing(n, finish));
                    finish.await();
                    break;
                case "dequeueing":
                    // enqueueing n
                    n = Integer.parseInt(args[1]);
                    finish = new CountDownLatch(1);
                    system.tell(new RootActor.HandleDequeueing(n, finish));
                    finish.await();
                    break;
                case "initiation":
                    n = Integer.parseInt(args[1]);
                    finish = new CountDownLatch(1);
                    system.tell(new RootActor.HandleInitiation(n, finish));
                    finish.await();
                    break;
                case "single-producer-sending":
                    n = Integer.parseInt(args[1]);
                    finish = new CountDownLatch(1);
                    system.tell(new RootActor.HandleSingleProducerSending(n, finish));
                    finish.await();
                    break;
                case "multi-producer-sending":
                    // multi-producer-sending n [p]
                    n = Integer.parseInt(args[1]);
                    parallelism = 10;
                    if (args.length > 2) {
                        parallelism = Integer.parseInt(args[2]);
                    }
                    finish = new CountDownLatch(1);
                    system.tell(new RootActor.HandleMultiProducerSending(n, parallelism, finish));
                    finish.await();
                    break;
                case "max-throughput":
                    // max-throughput n [p]
                    n = Integer.parseInt(args[1]);
                    parallelism = 10;
                    if (args.length > 2) {
                        parallelism = Integer.parseInt(args[2]);
                    }
                    finish = new CountDownLatch(1);
                    system.tell(new RootActor.HandleMaxThroughput(n, parallelism, finish));
                    finish.await();
                    break;
                case "ping-latency":
                    n = Integer.parseInt(args[1]);
                    finish = new CountDownLatch(1);
                    system.tell(new RootActor.HandlePingLatency(n, finish));
                    finish.await();
                    break;
                case "ping-throughput-10k":
                    n = Integer.parseInt(args[1]);
                    int pairCount = 10_000;
                    finish = new CountDownLatch(1);
                    system.tell(new RootActor.HandlePingThroughput(n, pairCount, finish));
                    finish.await();
                    break;
            }
        }
    }
}
