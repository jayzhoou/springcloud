package com.southwind.controller;

import com.southwind.entity.Student;
import com.southwind.fegin.FeginProviderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/fegin")
public class FeginHandler {
    @Autowired
    private FeginProviderClient feginProviderClient;

    @GetMapping("/findAll")
    public Collection<Student> findAll(){
        return  feginProviderClient.findAll();
    }
    @GetMapping("/index")
    public String index(){
        return  feginProviderClient.index();
    }
}

