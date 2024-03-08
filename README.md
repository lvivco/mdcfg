# Project Description
This project was initially developed by Avery & Softserve companies as an internal tool for their internal operations. It has now been open-sourced to encourage collaboration and contributions from the community.

# Overview
**mdcfg** is a **Multidimensional Configuration library for Java distributed apps** 

## Features:
* **[Easy to use](#quick-start)**
* **<details><summary>[Different source of configuration](#initialize-configuration-source)**</summary>
  * **[Hooks](#hooks)** 
  * **[Multi file source](#multi-file-source)**
  * **[Auto-reloads](#automatic-configuration-update)** 
* **<details><summary>[Multidimensional configuration](#multidimensional-configuration)**</summary>
  * **[Nested properties](#nested-properties)**
  * **[Numeric and range selectors](#numeric-and-range-selectors)**
  * **[Multi-Value Selectors](#multi-value-selectors)**
  * **Reference values**
  * **Aliases**
* **<details><summary>Response type casts**</summary>
    * **Cast to primitives**
    * **Custom type cast**
    * **Optionals**
* **<details><summary>Compound properties**</summary>
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
<details><summary>Usage</summary>

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

## Initialize configuration source
You can use yaml or json as configuration source.
#### **`Main.java`**
``` java
MdcProvider providerYaml = MdcBuilder.withYaml(YAML_PATH).build();
MdcProvider providerJson = MdcBuilder.withJson(JSON_PATH).build();
```
In the future we plan to add support for additional types of configuration sources, such as XML or HOCON formats, to provide more flexibility in managing application configurations.

## Hooks
Config builder allows you to use hooks to modify or process configuration values before they are returned by the library. This can be useful for tasks such as decrypting secret keys or transforming values based on certain conditions.
<details><summary>Example</summary>

### Decrypting AWS Secret Key
Hooks are functions that are called when reading properties from the configuration file, allowing you to modify or process the values. Builder allows you to add multiple hooks and also add hooks based on a pattern. For example:
#### **`Main.java`**
``` java
MdcProvider provider = MdcBuilder.withYaml(YAML_PATH)
        .loadHook("secret_aws_key", value -> decrypt(value))
        .loadHook(Pattern.compile("^aws_.*$"), value -> decrypt(value))
        .build();
```
</details>

## Multi file source
Initialize config with a path to the folder containing configuration files. Subfolders are not supported.
#### **`Main.java`**
``` java
MdcProvider provider = MdcBuilder.withYaml(CONFIG_FOLDER_PATH).build();
```
Also, MDC allows you to include the contents of other configuration files within your main configuration file. This feature can be useful for organizing and reusing configuration values across multiple files.
To include another configuration file, use the includes directive followed by the path to the file you want to include. For example:
#### **`config.yaml`**
``` yaml
includes:
  prices: price-conf.yaml
  aliases: aliases-conf.yaml
```

## Automatic Configuration Update
MDC provides a feature for automatically updating the configuration when the source file changes. This can be useful in situations where the configuration needs to be reloaded dynamically without restarting the application.

<details><summary>Usage</summary>

To enable automatic configuration update, use the autoReload method when building the MdcProvider. You can specify the interval at which the configuration file should be checked for changes and provide a callback function to handle the updated configuration.

#### **`Main.java`**
``` java
MdcProvider provider = MdcBuilder.withYaml("path/to/config.yaml")
                .autoReload(500, MdcCallback.<Integer, MdcException>builder()
                        .onSuccess(count -> {
                            // Handle updated configuration
                        })
                        .onFailure(ex -> {
                            // Handle update failure
                        })
                        .build())
                .build();
```
</details>

## Multidimensional configuration
Library enable you to define configuration values based on different dimensions or conditions, such as environment, platform, or any other business criteria relevant to your application. Here's how selectors work:

1. **Definition:** In your configuration file, you define configuration values within nested structures. Selectors are used to specify different values for different scenarios or dimensions.

2. **Syntax:** Selectors are defined using the **'@'** symbol followed by the dimension or condition. For example, **'environment@development'**, **'platform@ios'**, **'region@us'**, etc.

3. **Selection:** At runtime, the library evaluates the current context and selects the appropriate value based on the provided selector.

4. **Priority:** If multiple selectors match the current context, the selector with the most specific match takes precedence using priority given from bottom to top. 

5. **Fallback:** You can provide fallback values to handle cases where no selector matches the current context. This ensures that there's always a default value available, even if the specific conditions are not met.
<details><summary>Example</summary>

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

## Nested properties
The MDC library supports nested properties in the configuration, allowing you to organize related settings under a common hierarchy.
<details><summary>Example</summary>

#### **`config.yaml`**
``` yaml
project:
  name: "Awesome Project"
  type: "Web Application"
  stack:
    frontend: "React"
    backend: "Java"
    database: "MongoDB"
```
You can access nested properties using dot notation. For example, to retrieve the frontend framework:
#### **`Main.java`**
``` java
String frontend = provider.getString(TestContextBuilder.EMPTY, "project.stack.frontend");
```
</details>

## Numeric and range selectors
Numeric and range selectors in the configuration allow for defining different values or behaviors based on numeric conditions. These selectors are useful for scenarios where certain actions or settings depend on specific numeric ranges.

### Syntax
- **Numeric Selector: 'selector@value'**: Selects the value based on the exact numeric value of the selector.
- **Range Selector: 'selector@[start..end]'**: Selects the value for a range of values, including both the start and end values.
- **Excluding Range Selector: 'selector@[!start..end]'**: Selects the value for a range of values, excluding the start value but including the end value.

<details><summary>Example</summary>

#### **`config.yaml`**
``` yaml
final_grade:
  score@[..50]: "F"
  score@[!50..60]: "D"
  score@[!60..70]: "C"
  score@[!70..80]: "B"
  score@[!80..100]: "A"
```
These selectors provide a flexible way to define behavior or values based on numeric conditions, allowing for more dynamic and adaptable configurations.
</details>

## Multi-Value Selectors
In configurations, selectors can represent lists of values. These selectors are denoted with an asterisk (*) after the selector name. This feature is useful for scenarios where certain properties depend on combinations of values.

<details><summary>Example</summary>

Imagine a configuration for a software product where the subscription type is based on the selected features:
#### **`config.yaml`**
``` yaml
subscription:
  any@: basic
  features*@[encryption, audit-logging]: standart
  features*@advanced-analytics: premium
  features*@multi-tenancy: ultra
```
- Defines the default subscription level as "basic".
- **'features\*@[encryption, audit-logging]'**: Adds the "standard" subscription level if the "encryption" or "audit-logging" features are selected.
- **'features\*@advanced-analytics'**: Upgrades the subscription level to "premium" if the "advanced-analytics" feature is selected.
- **'features\*@multi-tenancy'**: Upgrades the subscription level to "ultra" if the "multi-tenancy" feature is selected.
- 
#### **`Main.java`**
``` java
MdcContext ctx = ContextBuilder.init().features(List.of("audit-logging")).build()
String subscription = provider.getString(ctx, "subscription");
```
This configuration provides flexibility in defining subscription levels based on the selected features.
</details>

# Contributing
To contribute the project follow [guide](CONTRIBUTING.md).

# License
Licensed under the MIT License. See LICENSE file.