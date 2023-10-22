package com.southwind.fegin.impl;

import com.southwind.entity.Student;
import com.southwind.fegin.FeginProviderClient;
import org.springframework.stereotype.Component;

import java.util.Collection;
@Component
public class FeginError implements FeginProviderClient {
    @Override
    public Collection<Student> findAll() {
        return null;
    }

    @Override
    public String index() {
        return "服务器维护中。。。";
    }
}
