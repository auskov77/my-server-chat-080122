package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.dao.UserDao;
import ru.itsjava.dao.UserDaoImpl;
import ru.itsjava.domain.User;
import ru.itsjava.utils.Props;

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
    String messageFromClient = "";
    boolean isAutho = false;
    boolean isReg = false;

    @SneakyThrows
    @Override
    public void run() {
        System.out.println("Client connected");
        // перед добавлением проверять регистрацию

        // чтобы считывать сообщение с клиента есть BufferedReader
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // чтобы считать что-то с клиента берем InputStream
        // будем считывать с помощью цикла while - писать бесконечно
        // сообщение от клиента

        isAutho = messageFromClient.equals("!autho!");
        isReg = messageFromClient.equals("!reg!");

        if (isAutho){
            authorization(bufferedReader);
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
        } else if (isReg){
            registration(bufferedReader);
            serverService.addObserver(this);
            while ((messageFromClient = bufferedReader.readLine()) != null){
                serverService.notifyObserverExceptMe(user.getName() + ":" + messageFromClient, this);
            }
        }

//        // проверка на то, что это авторизация или регистрация
//        if (authorization(bufferedReader)) {
//            // добавить Observer'a на сервере
//            serverService.addObserver(this);
//
//            // считываем readLine
//            while ((messageFromClient = bufferedReader.readLine()) != null) {
//                System.out.println(user.getName() + ":" + messageFromClient);
//                // с сервера отправляем сообщение всем
////                serverService.notifyObserver(user.getName() + ":" + messageFromClient);
//                // от клиента отправляем сообщение всем кроме себя
//                serverService.notifyObserverExceptMe(user.getName() + ":" + messageFromClient, this);
//            }
//        }
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
            if (authorizationMessage.startsWith("!autho!")){ // если authorizationMessage начинается с !autho!, подставляем login и password
                String login = authorizationMessage.substring(7).split(":")[0]; // substring - выделили подстроку, далее разбиваем строку по : методом split
                String password = authorizationMessage.substring(7).split(":")[1]; // substring - выделили подстроку, далее разбиваем строку по : методом split

                // должны у userDao вызывать соответствующий метод
                // новому пользователю присваиваем логин и пассворд
                user = userDao.findByNameAndPassword(login, password);
                return true; // все в порядке
            }
        }
        return false; // если false, то применить метод регистрации
    }

    // создаем метод регистрации
    @SneakyThrows
    private boolean registration(BufferedReader bufferedReader) {
        // сообщение от клиента
        String registrationMessage;
        // считываем registrationMessage с помощью bufferedReader.readLine
        while ((registrationMessage = bufferedReader.readLine()) != null) {
            // !reg!login:password
            // делаем проверку
            if (registrationMessage.startsWith("!reg!")){ // если registrationMessage начинается с !reg!, подставляем newLogin и newPassword
                String newLogin = registrationMessage.substring(5).split(":")[0]; // substring - выделили подстроку, далее разбиваем строку по : методом split
                String newPassword = registrationMessage.substring(5).split(":")[1]; // substring - выделили подстроку, далее разбиваем строку по : методом split

                // должны у userDao вызывать соответствующий метод
                // новому пользователю присваиваем логин и пассворд
                user = userDao.createNewUser(newLogin, newPassword);
                return true; // все в порядке
            }
        }
        return false;
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
