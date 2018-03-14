# Single Verticle Vert.x for Microservice
But with the power of kubernetes, the "Multiple Verticle" may not be suitable for running stateless Vert.x in microservice architecture because any stateless service can be scaled horizontally on demand.

# Methodology
Using Vert.x event-loop to process request and handle all request in kotlin coroutine(non-blocking & more efficient) 

# Benchmark 
https://github.com/gaplo917/web-framework-benchmark

# Thin wrapper to make Express-like API
Learning new web framework is pain and time-consuming.
 
Vert.x is incredibly flexible and performant. I started to use Vert.x Koltin to build stateless API Server and aim 
to build a express-like API web framework in Kotlin.

Thanks to Kotlin language design(extension & inline function), I can make a thin wrapper to wrap all vert.x components to
 make a `KExpress` without having any performance penalty. `KExpress` is designed for people who come from `expressjs`
 
This project is under active development for investigate express use case.
 
A separate project will be published as `KExpress` when I feel it is ready. Feel free to contribute by opening github issue

### Progress - KExpress API coverage
    TODO
