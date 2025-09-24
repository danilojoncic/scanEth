package com.dj.api.controller;

import com.dj.api.service.implementation.SampleServiceImplemented;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check")
@RequiredArgsConstructor
public class SampleController {

    private final SampleServiceImplemented sampleService;




    @GetMapping("/key")
    public ResponseEntity<?> checkLoadingOfApiKeyAndController(){
        return ResponseEntity.status(200).body(sampleService.getApiKey());
    }






}
