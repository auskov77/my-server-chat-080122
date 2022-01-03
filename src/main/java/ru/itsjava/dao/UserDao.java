package ru.itsjava.dao;

import ru.itsjava.domain.User;

public interface UserDao {
    User findByNameAndPassword (String name, String password);
    User createNewUser (String newName, String newPassword);
}
