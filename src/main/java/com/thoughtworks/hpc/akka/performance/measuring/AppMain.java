package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.ActorSystem;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class AppMain {
    public static void main(String[] notUsed) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        ActorSystem<RootActor.Command> system = ActorSystem.create(RootActor.create(), "akka-performance-measuring");
        int n;
        CountDownLatch finish;

        while (true) {
            System.out.print("> ");
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
            }
        }
    }
}
