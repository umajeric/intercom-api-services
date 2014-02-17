Intercom is a customer relationship management and messaging tool for web app owners

This library provides connectivity with the Intercom API (https://api.intercom.io)

Project is based on PHP version found here: https://github.com/nubera-ebusiness/intercom-php


Project is in development mode and as such not tested completely.


## Basic usage:

### Include Intercom Sources with Maven

```xml

<dependency>
		<groupId>si.majeric</groupId>
		<artifactId>intercom-api-services</artifactId>
		<version>1.0.0</version>
</dependency>
```


### Configure Intercom with your access credentials

```java

Intercom intercom = new Intercom("dummy-app-id", "dummy-api-key");

```

### Get all users

```java

Intercom intercom = new Intercom("dummy-app-id", "dummy-api-key");

JsonElement response = _intercom.getAllUsers(page, 100);
```

### Create a new user

```java
Intercom intercom = new Intercom("dummy-app-id", "dummy-api-key");

intercom.createUser("userId001", "email@example.com", null, null, System.currentTimeMillis(), null, null, null, null);

```

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/umajeric/intercom-api-services/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
