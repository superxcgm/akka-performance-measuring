package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;

import java.util.Scanner;

public class AppMain {

    private static void tellSync(ActorRef<RootActor.Command> rootActor, RootActor.Command command) throws InterruptedException {
        rootActor.tell(command);
        command.finish.await();
    }

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

                    tellSync(system, new RootActor.HandleEnqueueing(n));
                    break;
                case "dequeueing":
                    // enqueueing n
                    n = Integer.parseInt(args[1]);
                    tellSync(system, new RootActor.HandleDequeueing(n));
                    break;
                case "initiation":
                    n = Integer.parseInt(args[1]);
                    tellSync(system, new RootActor.HandleInitiation(n));
                    break;
                case "single-producer-sending":
                    n = Integer.parseInt(args[1]);
                    tellSync(system, new RootActor.HandleSingleProducerSending(n));
                    break;
                case "multi-producer-sending":
                    // multi-producer-sending n [p]
                    n = Integer.parseInt(args[1]);
                    parallelism = 10;
                    if (args.length > 2) {
                        parallelism = Integer.parseInt(args[2]);
                    }
                    tellSync(system, new RootActor.HandleMultiProducerSending(n, parallelism));
                    break;
                case "max-throughput":
                    // max-throughput n [p]
                    n = Integer.parseInt(args[1]);
                    parallelism = 10;
                    if (args.length > 2) {
                        parallelism = Integer.parseInt(args[2]);
                    }
                    tellSync(system, new RootActor.HandleMaxThroughput(n, parallelism));
                    break;
                case "ping-latency":
                    n = Integer.parseInt(args[1]);
                    tellSync(system, new RootActor.HandlePingLatency(n));
                    break;
                case "ping-throughput-10k":
                    n = Integer.parseInt(args[1]);
                    int pairCount = 10_000;
                    tellSync(system, new RootActor.HandlePingThroughput(n, pairCount));
                    break;
            }
        }
    }
}
