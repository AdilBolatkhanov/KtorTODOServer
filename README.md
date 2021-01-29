# KtorTODOServer
REST API backend server using Kotlin + Ktor + PostgreSQL + Exposed + HikariCP

## How it works
The application was built with:
* <b>Kotlin</b> as programming language
* <b>Ktor</b> - Kotlin async web framework
* <b>Netty</b> - Async web server
* <b>Exposed</b> - Kotlin SQL framework
* Implemented pagination in order to prevent loading large data sets and efficiently load and display small chunks of data at a time. Loading partial data on demand reduces usage of network bandwidth and system resources.
* <b>GSON</b> - serialize and deserialize Java objects to and from JSON.
* <b>PostgreSQL</b> - is a traditional RDBMS SQL database
* <b>Authentication JWT</b> - JSON Web Token – to create JSON-based access tokens.
* <b>HikariCP</b> - as datasource to abstract driver implementation
* <b>Sessions</b> - keeps track of the current user so you have a state associated with it
* <b>Locations and Routing</b> handle API routes.
