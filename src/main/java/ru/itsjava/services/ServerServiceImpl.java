package ru.itsjava.services;

import lombok.SneakyThrows;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//наш сервер
public class ServerServiceImpl implements ServerService, Observable {
    public final static int PORT = 8081; // порт, по которому подсоединяемся к нашему серверу
    public final List<Observer> observers = new ArrayList<>(); // массив, где хранятся наблюдатели - Observer'ы, те кто находится в чате

    @SneakyThrows // обработка исключений
    @Override
    public void start() {
        ServerSocket serverSocket = new ServerSocket(PORT); // настраиваем соединение - создаем serverSocket
        System.out.println("== SERVER STARTS ==");

        // постоянно слушаем порт сервера 8081
        while (true) { // бесконечный цикл ожидания
            // как проверить что клиент подключился - метод accept у socket'a
            Socket socket = serverSocket.accept();

            if (socket != null) { // если socket не пустой, т.е. подключился клиент
                // чтобы запустить отдельный поток пишем new Thread, передаем сюда new ClientRunnable, ClientRunnable зависит от socket'a
                // стартуем новый поток в ClientRunnable
                Thread thread = new Thread(new ClientRunnable(socket, this));
                thread.start(); // запускает новый поток
            }
        }

    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void deleteObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObserver(String message) {
        // пробежаться внутри ArrayList'a и всех про-notify
        for (Observer observer: observers){
            observer.notifyMe(message);
        }
    }

    @Override
    public void notifyObserverExceptMe(String message, Observer observer) {
        for (Observer value : observers) {
            if (!observer.equals(value)) {
                value.notifyMe(message);
            }
        }
    }
}
