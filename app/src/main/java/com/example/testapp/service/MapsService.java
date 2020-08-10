package com.example.testapp.service;

import android.util.Log;

import com.example.testapp.model.LocationObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class MapsService {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    public MapsService(){
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
    }


    public void postLocation() throws JsonProcessingException {
        String requestBody = "";
        try{
            requestBody = objectMapper.writeValueAsString(new LocationObject(1,1));
        } catch (JsonProcessingException e) {
            Log.d("postLocation", "Error in parsing requestBody");
            throw e;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = null;
        try {
            restTemplate.exchange("", HttpMethod.POST, request, String.class);
        } catch (RestClientException e){
            Log.d("postLocation","Error in getting response");
            throw e;
        }

        if(response.getStatusCode().is2xxSuccessful()){
            Log.d("postLocation","Upload of Location Success");
        }
    }

    public void getLocations(){

    }

}
