package ru.itsjava.domain;

public class UserIncorrectEntered extends RuntimeException{
    public UserIncorrectEntered(String message){
        super(message);
    }
}
