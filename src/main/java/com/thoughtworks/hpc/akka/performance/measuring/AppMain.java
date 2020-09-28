package com.thoughtworks.hpc.akka.performance.measuring;

import akka.actor.typed.ActorSystem;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class AppMain {
    public static void main(String[] notUsed) {
        Scanner scanner = new Scanner(System.in);

        ActorSystem<RootActor.Command> system = ActorSystem.create(RootActor.create(), "akka-performance-measuring");

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
                case "enqueueing":
                    // enqueueing n
                    int n = Integer.parseInt(args[1]);

                    CountDownLatch finish = new CountDownLatch(1);
                    RootActor.HandleEnqueueing handleEnqueueing = new RootActor.HandleEnqueueing(n, finish);
                    system.tell(handleEnqueueing);
                    try {
                        finish.await();
                    } catch (InterruptedException e) {
                        system.log().error(e.toString());
                    }
                    break;
                case "n":
                    System.out.println("hello world");
                    break;
            }
        }
    }
}
