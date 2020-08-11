package com.example.testapp.service;

import android.os.AsyncTask;
import android.util.Log;
import com.example.testapp.model.AreaInformation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.ArrayList;
import java.util.List;

public class MapsService extends AsyncTask<LatLng, Void, List<AreaInformation>> {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private static final String URL = "http://192.168.1.10:8080/";

    @Override
    protected void onPostExecute(List<AreaInformation> areaInformations) {
        super.onPostExecute(areaInformations);
    }

    @Override
    protected List<AreaInformation> doInBackground(LatLng... latLngs) {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        try {
            return postLocation(latLngs[0]);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<AreaInformation> postLocation(LatLng latLng) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(URL + "retrieveAreaInformation")
                .queryParam("longitude",latLng.longitude)
                .queryParam("latitude", latLng.latitude);
        ResponseEntity<JsonNode> response = null;
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        try {
            response = restTemplate.getForEntity(builder.build().encode().toUri(), JsonNode.class);
            Log.d("postLocation", objectMapper.writeValueAsString(response));
        } catch (RestClientException e){
            Log.d("postLocation","Error in getting response");
            throw e;
        }
        List<AreaInformation> areaInformations = new ArrayList<>();
        if(response.getStatusCode().equals(HttpStatus.OK)){
            Log.d("postLocation","Upload of Location Success");
            areaInformations = objectMapper.convertValue(response.getBody(), new TypeReference<List<AreaInformation>>() {});
        }

        return areaInformations;
    }

}
