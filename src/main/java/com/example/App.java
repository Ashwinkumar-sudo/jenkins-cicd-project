
package com.example;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class App {

    public static String getMessage() {
        return "Welcome to Jenkins CI/CD Pipeline!";
    }

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
            new InetSocketAddress("0.0.0.0", 8080), 0
        );

        server.createContext("/", exchange -> {

            String html = """
                <html>
                <head><title>Jenkins CI/CD Project</title></head>
                <body style="text-align:center;font-family:Arial;">
                <h1>Hello World!</h1>
                <h2>Welcome to Jenkins CI/CD Pipeline</h2>
                <p>Automatically deployed using Docker and AWS EC2</p>
                </body>
                </html>
                """;

            byte[] response = html.getBytes();

            exchange.getResponseHeaders().set(
                "Content-Type", "text/html; charset=UTF-8"
            );

            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        server.start();

        System.out.println("Application running on port 8080");
    }
}
