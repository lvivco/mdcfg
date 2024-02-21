# Project Description
This project was initially developed by Avery & Softserve companies as an internal tool for their internal operations. It has now been open-sourced to encourage collaboration and contributions from the community.

# Overview
**mdcfg** is a **Multidimensional Configuration library for Java distributed apps** 

## Features:
* **[Easy to use](#quick-start)**
* **Different source of configuration**
  * **Hooks** 
  * **Multi file source**
  * **Auto-reloads** 
* **[Multidimensional configuration](#multidimensional-configuration)**
  * **Nested properties**
  * **Range selectors**
  * **List selectors**
  * **Reference values**
  * **Aliases**
* **Response type casts**
    * **Cast to primitives**
    * **Custom type cast**
    * **Optionals**
* **Compound properties**
    * **Cast to Map**
    * **Cast to JSON**
    * **Cast to custom object**
    * **Cast to list of custom objects**

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
<details>
  <summary>Usage</summary>

Create config file for example:
#### **`config.yaml`**
``` yaml
my-property: 100
```
Add the following code to your application that loads configuration:
```java
public class MdcfgPoweredApplication {
  public static void main(String... args) {
    MdcProvider provider = MdcBuilder.withYaml("config.yaml").build();
    Integer configValue = provider.getInteger(ctx, "my-property");
    // Use it!
    System.out.println(configValue);
  }
}
```
</details>

## Multidimensional configuration
Library enable you to define configuration values based on different dimensions or conditions, such as environment, platform, or any other business criteria relevant to your application. Here's how selectors work:

1. **Definition:** In your configuration file, you define configuration values within nested structures. Selectors are used to specify different values for different scenarios or dimensions.

2. **Syntax:** Selectors are defined using the **'@'** symbol followed by the dimension or condition. For example, **'environment@development'**, **'platform@ios'**, **'region@us'**, etc.

3. **Selection:** At runtime, the library evaluates the current context and selects the appropriate value based on the provided selector.

4. **Priority:** If multiple selectors match the current context, the selector with the most specific match takes precedence using priority given from bottom to top. 

5. **Fallback:** You can provide fallback values to handle cases where no selector matches the current context. This ensures that there's always a default value available, even if the specific conditions are not met.
<details>
  <summary>Example</summary>

#### **`config.yaml`**
``` yaml
database:
  type: "mysql"
  connection:
    any@: "default-connection"
    environment@production: "prod-connection"  
    environment@development: 
      any@: "dev-connection" 
      platform@ios: "dev-ios-connection"
      platform@android: "dev-android-connection"

```
### Selectors Explained:
1. **'any@' Selector:**
   * Represents the default value when no specific selector matches.
   * Example: **"default-connection"** for **'connection'**.
2. **'environment@' Selector:**
    * Specifies values based on environments (e.g., development or production).
    * Example: **"prod-connection"** for **"production"** environment.
3. **'platform@' Selector:**
   * Allows specifying values based on platforms (e.g., Android or iOS).
   * Example: **"dev-android-connection"** for **"android"** platform.


### Example Scenario:
#### Given context:

* **"environment": "development"**
* **"platform": "ios"**

When retrieving **'connection'**, MDC follows these steps:

1. Checks for a match for **"development"** environment and **"ios"** platform. Retrieves **"dev-ios-connection"** if found.
2. If not, checks for a match only for **"development"**. Retrieves **"dev-connection"** if found.
3. Falls back to **"default-connection"** if no match is found.
</details>

# Contributing
To contribute the project follow [guide](CONTRIBUTING.md).

# License
Licensed under the MIT License. See LICENSE file.