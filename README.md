# Project Description
This project was initially developed by Avery & Softserve companies as an internal tool for their internal operations. It has now been open-sourced to encourage collaboration and contributions from the community.

# Overview
**MDC (Multidimensional Configuration library for Java distributed apps)**

The library focuses on managing configurations in a multidimensional manner, allowing developers to handle complex configuration setups with ease. The core concept of MDC revolves around organizing configurations along multiple dimensions, such as environment-specific settings, feature toggles, and application modes. This approach enables developers to create more flexible and dynamic configurations that can adapt to different contexts and requirements.

### Key Features
* **Multidimensional Configuration:** Organize configurations along multiple dimensions, such as environment, feature sets, and application modes.
* **Flexible Configuration Loading:** Load configurations from various sources, including files, databases, and external services.
* **Dynamic Configuration Updates:** Support for live updates and dynamic reloading of configurations.
* **Rich set of methods for reading configuration values:** Users can choose from various methods based on their needs, such as getting a single value, a list of values, or even complex objects.
* **Customizable Configuration Handling:** Implement custom logic for processing and modifying configurations based on application needs.


## Quick start
### Setting up dependency
#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.lvivco</groupId>
        <artifactId>mdcfg</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```
### Usage
Create config file for example:
#### **`config.yaml`**
``` yaml
database:
  url:
    env@development: "jdbc:mysql://localhost:3306/dev_db"
    env@production:
      region@us-west: "jdbc:mysql://prod-db-us-west.example.com:3306/prod_db"
      region@us-east: "jdbc:mysql://prod-db-us-east.example.com:3306/prod_db"
  username:
    env@development: "dev_user"
    env@production:
      region@us-west: "prod_user_us_west"
      region@us-east: "prod_user_us_east"
  password:
    env@development: "dev_password"
    env@production:
      region@us-west: "prod_password_us_west"
      region@us-east: "prod_password_us_east"
  maxConnections: 10
```
Add the following code to your application that loads configuration:
```java
public class MdcfgPoweredApplication {
  public static void main(String... args) {
      // load config
      MDCConfig config = MDCConfig.builder()
              .source("config.yaml")
              .build();
      
      // Define a context for the configuration
      MdcContext context = new MdcContext();
      context.put("environment", "production");
      context.put("region", "us-west");

      // Retrieve a configuration value using the context
      String databaseUrl = config.getString(context, "database.url");
      String databaseUsername = config.getString(context, "database.username");
      String databasePassword = config.getString(context, "database.password");
      int maxConnections = config.getInt(context, "database.maxConnections");

      // use it
  }
}
```

## Learn about MDC
Our users' guide, [MDC Explained](https://github.com/lvivco/mdcfg/wiki)

# Contributing
To contribute the project follow [guide](CONTRIBUTING.md).

# License
Licensed under the MIT License. See LICENSE file.