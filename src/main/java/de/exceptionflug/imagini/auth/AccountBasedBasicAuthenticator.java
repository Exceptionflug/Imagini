package de.exceptionflug.imagini.auth;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpExchange;
import de.exceptionflug.imagini.ImaginiServer;
import de.exceptionflug.imagini.config.Account;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.concurrent.locks.ReentrantLock;

public class AccountBasedBasicAuthenticator extends BasicAuthenticator {

    private final ImaginiServer imaginiServer;
    private final ReentrantLock lock = new ReentrantLock();
    private Account account;

    public AccountBasedBasicAuthenticator(ImaginiServer imaginiServer) {
        super("imagini");
        this.imaginiServer = imaginiServer;
    }

    @Override
    public Result authenticate(HttpExchange httpExchange) {
        try {
            lock.lock();
            String host = httpExchange.getRequestHeaders().getFirst("Host");
            account = imaginiServer.getAccountByAddress(host);
            if(account == null) {
                return new Failure(404);
            }
            return super.authenticate(httpExchange);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean checkCredentials(String user, String pass) {
        if(account.getPasswordHash() == null && account.getName().equalsIgnoreCase(user)) {
            account.setPasswordHash(DigestUtils.sha1Hex(pass));
            imaginiServer.saveConfig();
            return true;
        }
        return account.getName().equalsIgnoreCase(user) && account.getPasswordHash().equals(DigestUtils.sha1Hex(pass));
    }

}
