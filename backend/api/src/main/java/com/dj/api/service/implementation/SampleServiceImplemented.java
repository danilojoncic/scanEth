package com.dj.api.service.implementation;

import com.dj.api.service.SampleService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Data
public class SampleServiceImplemented implements SampleService {


    @Value("${etherscan.api.key}")
    private String apiKey;

}
