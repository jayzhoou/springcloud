package com.southwind.fegin;

import com.southwind.entity.Student;
import com.southwind.fegin.impl.FeginError;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
@FeignClient(value = "provider",fallback = FeginError.class)
public interface FeginProviderClient {
    @GetMapping("/student/findAll")
    public Collection<Student> findAll();
    @GetMapping("/student/index")
    public String index();
}
