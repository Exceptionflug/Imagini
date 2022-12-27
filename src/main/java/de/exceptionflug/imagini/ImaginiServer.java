package de.exceptionflug.imagini;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import de.exceptionflug.imagini.application.*;
import de.exceptionflug.imagini.auth.AccountBasedBasicAuthenticator;
import de.exceptionflug.imagini.config.Account;
import de.exceptionflug.imagini.config.Configuration;
import de.exceptionflug.moon.WebApplication;
import de.exceptionflug.moon.rest.RestApplication;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.InetSocketAddress;

@Getter
@Log4j2
public class ImaginiServer {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final RestApplication webApplication;
    private final Configuration configuration;

    private ImaginiServer() throws IOException {
        configuration = gson.fromJson(new FileReader("config.json"), Configuration.class);
        webApplication = RestApplication.quickStart(new InetSocketAddress(configuration.getBindingAddress(),
                configuration.getPort()), 128, "/");

        // Uploader
        webApplication.registerPageHandler("upload", new UploadHandler(this));

        // Default handler for file access
        webApplication.setDefaultPageHandler(new FileHandler(this));

        // Gallery
        webApplication.registerPageHandler("gallery", new GalleryHandler(this));
        webApplication.registerPageHandler("api/gallery", new GalleryApiHandler(this));
        webApplication.protect("gallery", new AccountBasedBasicAuthenticator(this));
        webApplication.protect("api/gallery", new AccountBasedBasicAuthenticator(this));

        log.info("Started imagini file server by Nico Britze");
    }

    public static void main(String[] args) throws IOException {
        File config = new File("config.json");
        if(!config.exists()) {
            saveResourceToFile("config.json", config);
        }
        new ImaginiServer();
    }

    private static void saveResourceToFile(String resource, File target) {
        try {
            target.createNewFile();
        } catch (IOException e) {
            System.out.println("Cannot copy resource");
            e.printStackTrace();
        }
        try(FileOutputStream fileOutputStream = new FileOutputStream(target)) {
            fileOutputStream.write(ByteStreams.toByteArray(ImaginiServer.class.getResourceAsStream("/" + resource)));
            fileOutputStream.flush();
        } catch (Exception e) {
            System.out.println("Cannot copy resource");
            e.printStackTrace();
        }
    }

    public Account getAccountByAddress(String address) {
        for(Account account : configuration.getAccounts()) {
            if(account.getAddress().equalsIgnoreCase(address)) {
                return account;
            }
        }
        return null;
    }

    public Account getAccountByLastAccessAddress(String address) {
        for(Account account : configuration.getAccounts()) {
            if(account.getLastAccessAddress() == null) {
                continue;
            }
            if(account.getLastAccessAddress().equalsIgnoreCase(address)) {
                return account;
            }
        }
        return null;
    }

    public void saveConfig() {
        try {
            if(configuration == null) {
                return;
            }
            try(FileWriter writer = new FileWriter("config.json")) {
                gson.toJson(configuration, writer);
            }
        } catch (IOException e) {
            System.out.println("Cannot save config");
            e.printStackTrace();
        }
    }

    public String getRemoteAddress(HttpExchange exchange) {
        String xff = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if(xff == null) {
            xff = exchange.getRemoteAddress().getHostString();
        }
        return xff;
    }

}
