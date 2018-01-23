# Quotes

![alt tag](medias/quotes.png)

## Features

Implements a simple rest quote service.

## Examples accessible from a browser

- `http://localhost:8080/quotes`
- `http://localhost:8080/quotes?author=jacobs&author=annie&quote=bad`
- `http://localhost:8080/quotes?author=bancroft`
- `http://localhost:8080/quotes.txt`
- `http://localhost:8080/quotes?text=Tiggers%20don%27t%20like%20honey.%20To%20the%20uneducated,%20an%20A%20is%20just%20three%20sticks.`

### Query parameters

- `author` finds authors name containing the parameter.
- `quote` finds quotes containing the parameter.
- `text` finds quotes included in the parameter.

## Implementation 

This demo application is written in scala with akka-http which get his data from
text file and stream the response.

## Execution 

- `sbt run`

## Requirements 

- Java 1.8
- Scala 2.12.4
- SBT 1.0.1

