package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.domain.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@RequiredArgsConstructor
// запуск нового пользователя - за это отвечает ClientRunnable
// это сущность клиента
public class ClientRunnable implements Runnable, Observer {
    private final Socket socket;
    private final ServerService serverService;
    private User user; // это наш пользователь

    @SneakyThrows
    @Override
    public void run() {
        System.out.println("Client connected");
        // перед добавлением проверять регистрацию

        // чтобы считывать сообщение с клиента есть BufferedReader
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // чтобы считать что-то с клиента берем InputStream
        // будем считывать с помощью цикла while - писать бесконечно
        // сообщение от клиента
        String messageFromClient;

        // проверка на то, что это авторизация или регистрация
        if (authorization(bufferedReader)) {
            // добавить Observer'a на сервере
            serverService.addObserver(this);

            // считываем readLine
            while ((messageFromClient = bufferedReader.readLine()) != null) {
                System.out.println(user.getName() + ":" + messageFromClient);
                // с сервера отправляем сообщение всем
//                serverService.notifyObserver(user.getName() + ":" + messageFromClient);
                // от клиента отправляем сообщение всем кроме себя
                serverService.notifyObserverExceptMe(user.getName() + ":" + messageFromClient, this);
            }
        }
    }

    // создаем метод авторизации
    @SneakyThrows
    private boolean authorization(BufferedReader bufferedReader) {
        // сообщение от клиента
        String authorizationMessage;
        // считываем authorizationMessage с помощью bufferedReader.readLine
        while ((authorizationMessage = bufferedReader.readLine()) != null) {
            // !autho!login:password
            // делаем проверку
            if (authorizationMessage.startsWith("!autho!")){ // если authorizationMessage начинается с !autho!, подсиавляем login и password
                String login = authorizationMessage.substring(7).split(":")[0]; // substring - выделили подстроку, далее разбиваем строку по : методом split
                String password = authorizationMessage.substring(7).split(":")[1]; // substring - выделили подстроку, далее разбиваем строку по : методом split
                // новому пользователю присваиваем логин и пассворд
                user = new User(login, password);
                return true; // все в порядке
            }
        }
        return false; // если false, то применить метод регистрации
    }

    @SneakyThrows
    @Override
    // метод notifyMe пишет конкретному Oserver'y, т.е. пишет сообшение клиенту
    public void notifyMe(String message) {
        // отправка сообщения с сервера клиенту c помощью PrintWriter'a
        PrintWriter clientWriter = new PrintWriter(socket.getOutputStream()); // OutputStream - отдаем
        clientWriter.println(message); // message - это наше сообщение
        clientWriter.flush();
    }
}
