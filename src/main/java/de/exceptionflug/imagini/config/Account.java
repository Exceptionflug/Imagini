package de.exceptionflug.imagini.config;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Account {

    private String name;
    private String address;
    private String redirectUrl;
    private String accessToken;
    private String passwordHash;
    private String lastAccessAddress;

}
