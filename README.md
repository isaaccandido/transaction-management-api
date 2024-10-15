# Transaction Management API

This API was built as part of a technical challenge.

# Overview

The Transaction Management API is designed to handle purchase transactions, allowing users to store purchase data
and retrieve transactions in specified currencies based on exchange rates. The API interacts with the Treasury Reporting
Rates of Exchange API to fetch historical exchange rates for currency conversion within a 6-month period from the
transaction date.

## Key Features:

* Store purchase transactions including description, transaction date, and purchase amount in USD.
* Retrieve transactions in a specified foreign currency, converted using the latest available exchange rates within a
  6-month window.
* Validation of transaction data with rules for valid date formats, positive amounts, and descriptions capped at 50
  characters.
* Automatic rounding of converted amounts to two decimal places.
* Robust error handling when no valid exchange rate is available within the required timeframe.
* Paginated endpoint for retrieving the list of stored transactions for efficient retrieval of large datasets.
* Expose an endpoint to manually trigger cache refresh (if caching is enabled), with the added ability to integrate with
  an external scheduler for automated cache management.

--- 

# Technical Specifications

* JDK Version: 21
* Maven Version: 3.9.9
* SpringBoot 3.3.4
* For detailed dependency and build configurations, refer to the [pom.xml](pom.xml) file.

# Running the Application

This API is packaged as a standalone Java application. To run the application on your machine, use the provided scripts
based on your operating system. It is designed to run within a directory without modifying system configurations
(like system variables), except for creating a .m2 directory in the user's root directory.

## Windows:

Run the batch script: ```run-project-windows.bat```

## Linux:

First, make the script executable: ```chmod +x run-project-linux.sh```

Then run the script: ```./run-project-linux.sh```

**Note:** The script handles the local Java environment setup, ensuring JDK 21 is used without requiring a global
installation of Java on your system.

--- 

# API Documentation

* All documentation to aid API usage, along with examples and required parameters, is available at the Swagger endpoint
  ```{BASE_ADDRESS}/api/v1/swagger-ui/index.html```
* Using actuator we can have some metrics. The API health check via ```{BASE_ADDRESS}/api/v1/actuator/health```.
  Additionally, I've enabled all actuator endpoints, since this isn't protected at all, but in a secure context, this
  should be changed.

# Code Testing

This project is designed with functional automated tests to ensure that the API meets production-level standards. To run
the tests, use Maven: ```mvn test```. The aforementioned scripts automate this part too, so it won't build or run if any
tests fail.

Please note that the full coverage report is attached. Refer to [Unit test reports](reports) directory (requires 
browser, open ```index.html```.).

# External Dependencies

* Treasury Reporting Rates of Exchange API:
  ```https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange```
  This external service is used to fetch exchange rates. More information can be found at the API documentation page for
  the Treasury Reporting Rates of Exchange website, [here](https://fiscaldata.treasury.gov/api-documentation/).

---

# Development Considerations:

## Security Considerations - IMPORTANT!

Please note that storing database credentials in configuration files is generally not recommended. This approach has
been used here solely for the purposes of this exercise, as the database is an in-memory instance. In a real-world
scenario, credentials would be securely managed, likely using Spring Cloud integrated with a Vault server to store
them on a secure cloud service. Thank you for your understanding.

Furthermore, Security measures, such as protecting endpoints and implementing JWT authentication, can be easily
integrated without requiring significant changes to the overall structure.

## Database Solution

It was selected the H2 in-memory database to maintain a simple and defined database implementation. The H2 console can
be accessed via a web browser at {BASE_URL}/api/v1/h2-console, with credentials specified in the application.yaml file.
This choice ensures full JPA functionality while enabling seamless transitions to other database providers with minimal
adjustments, if required.

## Architecture:

It was chosen the MVC architecture pattern for a number of reasons. One being that it is very well-known and
widely-used, so it is easier to understand and maintain. Code is for people, so people must be able to read it.
Therefore, any improvement needs should be simpler to implement due to an ease to identify where specific parts are
situated while still being quite friendly.

Furthermore, this architecture is easily scalable, meaning that it is possible to add features that are
independent of each other, making use of spring-managed beans to be injected wherever they are needed.

As for configuration, since the application itself is fairly small, there are just a few possible entries to configure.
However, keeping in mind that this is a scalable-ready application while not losing sight of its current objective, that
is, a SDE assessment, the application makes use of .yml configuration file instead of the default one, since it makes
for easier visualization and maintenance. There are some entries, such as the third-party API, that were found to be
useful as externalized, rather than hardcode.

## Implementation Details

### Endpoint Documentation Solution

Swagger has been populated with usage examples and descriptions where relevant, while maintaining a concise format.
Extensive use of annotations was applied to ensure a simple and straightforward user experience.

### Controller Considerations

It feels a little cluttered when you end up using a lot of annotations, so things were kept following a simple standard.
Controllers contain minimal logic to help maintain code clarity.

## Service Layer Considerations

All calls in service layer were thought to be simple. So, wherever logic could get messy, there is a delegation to
sub-functions. Similarly, all calls to the outside source were delegated to a gateway service, which is a sub-service
responsible only for network communications to the fiscal API. This helps keep the logic isolated while ensuring the
main service logic remains focused on internal processing.

## Mapping Considerations

It was considered using automatic model mapping for this exercise. However, AutoMapper has a vulnerability reported
by [MVN Repository](https://mvnrepository.com/artifact/io.bfil/automapper_2.13/0.7.0). Others, like MapStruct, did not
work very well, most like due to a SpringBoot version.
Given the small scope of this implementation, manual mapping was preferred. For future versions, automating this
process could be beneficial to reduce effort as the application scales. However, this could be replaced easily, with
little code modification.

### Overall Considerations

Annotations were widely used, such as anti boilerplate code, validations and annotations to delegate bean management to
Spring. This further contributes to keeping the code clean.

Edge cases were also considered. For example, while the application ensures valid data formatting, data accessed outside
the application might not adhere to this, which is addressed in the retrieval process. It helps
prevent errors with required data when databases are shared between systems, manually intervened or unforeseen defects.
So, no operation is done without proper check to ensure robustness.

### Performance Results

Additionally, application was tested under heavy load (~240 exchange requests per second). It was tested using Postman
(see attached [performance reports](/reports/performance)). Even under such stress, no memory leaks were to be found. 
This analysis was done using JProfiler. However, around 30% of exchange requests still fail due to sequential calls to 
external API, which is expected due to high load.

# Improvements For Hypothetical Future Releases

### Caching system

A cache system could significantly improve performance by reducing reliance on real-time API calls. While a proof of
concept was developed, time constraints prevented its full integration. It reduced error rate to 0% (as opposed to the
33% of errors) observed in exchange operations, mentioned [here](#Performance-Results). The proposed approach includes:

1. **Pre-fetching Exchange Data:** Upon application startup, all necessary exchange rates would be fetched and stored
   in a cache, reducing the need for frequent API calls.

2. **Asynchronous Updates:** A background service would periodically refresh the cache at configurable intervals,
   ensuring that exchange data remains up to date.

3. **Fallback Logic:** If a cache lookup fails (e.g., missing data within the 6-month window), the system would
   fallback to real-time API requests. This strategy reduces external API failures while maintaining data accuracy.
   Although this increases memory usage slightly (approximately 10MB based on tests), it provides faster response times,
   improves reliability, and reduces external service dependency.

In the future, we could also consider persisting cache data in a database to balance memory usage and query performance.

### Pagination For "/all" Endpoint

To improve scalability, adding pagination to the /all endpoint will optimize the retrieval of large datasets.
This would limit the size of the data returned in each response, enhancing both performance and usability.

Pagination will also reduce server load, allowing more efficient REST communication for clients handling large
transaction histories.

---

# Performance Updates And New Implementations (Update 12/10/2024)

### **Caching System**

The cache system was successfully implemented. Gains observed:

1. It reduced the error rate from around 30% to a consistent 0%. This is due solely because it's not needed to fetch
data from server everytime.
2. Dropped the average exchange rate from ~25 seconds to 3 milliseconds.
3. Also improved other endpoints since application overall load is lower. 
4. It still allows for requests to server if no cache is available and still fails as expected if server is fully
   unavailable.

Every result is available on the performance reports attached to the project, as linked below.

- [[PDF] Performance with cache disabled](reports/performance/cache_disabled.pdf)
- [[PDF] Performance with cache enabled](reports/performance/cache_enabled.pdf)

### **"Pagination For "/all" endpoint**

The pagination system was implemented. Gains observed:

1. Pages are less cluttered and allows for better REST communication.
2. Increased endpoint performance due to having a fixed amount of data being received.
3. Allows for better user experience with the API solution.

### **Manual Cache Refresh Endpoint**

This implementation allows external requests to trigger a cache refresh. An endpoint is exposed for manually triggering
the refresh, provided caching is enabled. The goal of this feature is to achieve the following:

1. Allow for manual interaction with internal caching mechanics.
2. The ability to integrate with an external scheduler for automated cache management.

--- 

# Thanks for the opportunity!
