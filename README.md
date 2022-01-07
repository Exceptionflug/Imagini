# Imagini
A java file server built on the moon library

## Purpose
You are using an screenshot capture tool like [ShareX](https://getsharex.com/) and don't want to use public cloud services for hosting your uploaded images and files? Then you should consider using Imagini. It's an on-premises http file server supporting direct use with ShareX and other uploaders where you can customize the upload target.

## Features
### Gallery
Imagini features a paginated and password protected gallery where you can access all your uploaded files. It is accessible using the `/gallery` endpoint.
![Gallery](https://img.exceptionflug.de/CId5lSk7FU.png)
#### Delete URL
You can delete uploaded pictures using a delete link. The delete URL looks like this: `http://your-domain.com/delete/<filename>`. Your gallery password is needed to delete images.
## Installation
Your server needs to run at least Java 8. Just simply run the imagini-server jar file. It will automatically create an example configuration.

### Using Docker

You can also use the provided docker image for easy deployment: https://hub.docker.com/r/exceptionflug/imagini

docker-compose.yml:
```yml
version: "3.7"
services:
  imagini:
    image: exceptionflug/imagini
    restart: unless-stopped
    ports:
      - "8080:80"
    volumes:
      - "./data/imagini:/opt/imagini"
```

## Configuration
### Server
config.json:
```xml
{  
  "bindingAddress": "0.0.0.0", <--- Address under which the service is accessible 
  "port": 8080, <--- Port under which the service is accessible
  "accounts": [ <--- An array containing multiple accounts
    {
      "redirectUrl": "http://localhost:8080/", <--- The URL which will be sent back to the uploader client (must have a trailing /)
      "address": "localhost:8080", <--- The domain name (with port if not port 80 / 443) of the server for this account
      "name": "default" <-- Used for logging
      "accessToken": "kjfrt352351" <--- optional security token that must be used when uploading a file
    }  
  ]
}
```
#### Example with multiple accounts, multiple domain names and HTTPS via reverse proxy
```xml
{  
  "bindingAddress": "85.63.7.190",
  "port": 8080,
  "accounts": [
    {
      "redirectUrl": "https://img.exceptionflug.de/",
      "address": "img.exceptionflug.de",
      "name": "Exceptionflug",
      "accessToken": "ofehw4n48f24f"
    },
    {
      "redirectUrl": "https://img.liondev.eu/",
      "address": "img.liondev.eu",
      "name": "LionDev",
      "accessToken": "aldfskof23104534"
    },
  ]
}
```
In this example our service is hosting two accounts using two different domain names. Since HTTPS is configured, Imagini must run behind a reverse proxy server (nginx or other). The reverse proxy will redirect all requests to `85.63.7.190:8080` without relocating the client. The account of the uploader is determined by the used address of the request. If a client attempts to request https://img.exceptionflug.de/upload the account of Exceptionflug will be used. If some client calls https://img.liondev.eu/upload the account of LionDev will be used. This is an automatic behaviour and cannot be disabled.
### Client
#### General
Please make sure you are using http method PUT for your URL suffixing `/upload`. The request body must contain the file to be uploaded. The file name of the file is specified in the request header field `File-Name`. If an access token is configured for that account, please specify it using the header field `Access-Token`.
#### ShareX
Go to custom target settings and create a new custom upload target:
![Settings](https://img.exceptionflug.de/ShareX_NxvEquDFWe.png)
#### Gallery
As soon as an account have been created, it should be secured with a password. To do this, navigate to the gallery page of this account. You are prompted to fill in the authentication details.
![set password](https://img.exceptionflug.de/msedge_1FA5UeBUHd.png)

As username enter the account name specified in the `config.json`. As password you can enter anything you want. This will set your password. If you want to reset your gallery password, just delete the `passwordHash` property from the account in the `config.json` file.
