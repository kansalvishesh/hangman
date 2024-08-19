package com.hangman.apiManager;

import com.hangman.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class DictionaryApiManager {
    @Autowired
    private RestTemplate restTemplate;

    public void validateWord(String word){
        String apiUrl = Utils.DICT_API_URL + word;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Word validation failed. The provided word is not valid.");
            }
        } catch (
                HttpClientErrorException e) {
            throw new RuntimeException("Word validation failed: " + e.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while validating the word.");
        }
    }
}
