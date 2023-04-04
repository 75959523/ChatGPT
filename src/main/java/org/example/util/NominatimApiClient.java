package org.example.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class NominatimApiClient {

    private static final String NOMINATIM_API_BASE_URL = "https://nominatim.openstreetmap.org";

    public String search(String query) {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = UriComponentsBuilder.fromHttpUrl(NOMINATIM_API_BASE_URL)
                .path("/search")
                .queryParam("q", query)
                .queryParam("format", "json")
                .build().toUri();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        return response.getBody();
    }

    public String reverseGeocode(double lat, double lon, int zoom) {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = UriComponentsBuilder.fromHttpUrl(NOMINATIM_API_BASE_URL)
                .path("/reverse")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("zoom", zoom)
                .queryParam("format", "json")
                .build().toUri();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        return response.getBody();
    }
}