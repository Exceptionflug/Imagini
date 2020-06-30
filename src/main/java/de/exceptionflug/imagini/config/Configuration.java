package de.exceptionflug.imagini.config;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class Configuration {

    private List<Account> accounts = new ArrayList<>();
    private String bindingAddress;
    private int port;

}
