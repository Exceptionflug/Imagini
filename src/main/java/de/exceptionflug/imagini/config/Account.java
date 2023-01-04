package de.exceptionflug.imagini.config;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class Account {

    private String name;
    private String address;
    private List<String> addresses = new ArrayList<>();
    private String redirectUrl;
    private String accessToken;
    private String passwordHash;
    private String lastAccessAddress;
    private boolean disallowLogDisable;

}
