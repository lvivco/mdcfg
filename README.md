## Project Description
This project was initially developed by Avery & Softserve companies as an internal tool for their internal operations. It has now been open-sourced to encourage collaboration and contributions from the community.

# Overview
**mdcfg** ("Multi dimensional configuration") is a **Multi dimensional configuration library for Java distributed apps** 

#### Features:
* Open source
* **Easy to use**
* **Multi dimensional** configuration (read values by key and multi dimension context provided)
* **Multi file source** of configuration
* **Auto-reloads** configuration
* **Powerful** configuration mechanisms

## Quick start
### Setting up dependency
#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.lvivco</groupId>
        <artifactId>mdcfg</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Usage
* Create config file for example config.yaml:
```yaml
horsepower:
  any@: 300
  model@bmw: 500
  model@toyota: 350
  model@ford: 340
```

* Use the following code in your application to connect to sample configuration source:
```java
public class MdcfgPoweredApplication {

  public static void main(String... args) {
    MdcProvider provider = MdcBuilder.withYaml("config.yaml")
            .autoReload()
            .build();

    MdcContext ctx = new MdcContext();
    ctx.put("model", "bmw");
    
    Integer configValue = provider.getInteger(ctx, "horsepower");
    // Use it!
    System.out.println(configValue);
  }

}
```

# Contributing
To contribute the project follow [guide](CONTRIBUTING.md).

# License
Licensed under the MIT License. See LICENSE file.