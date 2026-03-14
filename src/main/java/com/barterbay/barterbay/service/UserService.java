package com.barterbay.barterbay.service;

import com.barterbay.barterbay.model.User;
import com.barterbay.barterbay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    public String register(User user){

        if(repo.findByUsername(user.getUsername()) != null){
            return "User already exists";
        }

        repo.save(user);
        return "Account Created";
    }

    public String login(User user){

        User dbUser = repo.findByUsername(user.getUsername());

        if(dbUser == null){
            return "User not found";
        }

        if(!dbUser.getPassword().equals(user.getPassword())){
            return "Wrong password";
        }

        return "Login successful";
    }
}