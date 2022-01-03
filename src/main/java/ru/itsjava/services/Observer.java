package ru.itsjava.services;

// наблюдатель
public interface Observer {
    void notifyMe(String message); // уведомлять, рассылать сообщение самому себе
}
