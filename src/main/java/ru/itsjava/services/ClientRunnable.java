package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.dao.UserDao;
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
    private final UserDao userDao; // подключили сюда и еще проинициализировали
//    public String messageAuthorizationUser;
//    public String messageRegistrationUser;

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

        // проверка на авторизацию и регистрацию
        if ((messageFromClient = bufferedReader.readLine()) != null && messageFromClient.startsWith("!autho!")){
            String login = messageFromClient.substring(7).split(":")[0];
            String password = messageFromClient.substring(7).split(":")[1];
            user = userDao.findByNameAndPassword(login, password);
            notifyMe("Вы успешно авторизовались!");
        } else if ((messageFromClient.startsWith("!reg!"))) {
            String newName = messageFromClient.substring(5).split(":")[0];
            String newPassword = messageFromClient.substring(5).split(":")[1];
            user = userDao.createNewUser(newName, newPassword);
            notifyMe("Вы успешно зарегистрированы!");
        }
        serverService.addObserver(this);
//    }

    // начинаем цикл, где проверяем сообщение от клиента
        while((messageFromClient =bufferedReader.readLine())!=null){
        System.out.println(user.getName() + ":" + messageFromClient);
        // с сервера отправляем сообщение всем
//                serverService.notifyObserver(user.getName() + ":" + messageFromClient);
        // от клиента отправляем сообщение всем кроме себя
        serverService.notifyObserverExceptMe(user.getName() + ":" + messageFromClient, this);
    }
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
