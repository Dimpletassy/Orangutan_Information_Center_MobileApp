# Orangutan Information Center MobileApp

A mobile app developed for the Orangutan Information Centre (OIC) to support an automated plant watering system using hydraulic drums. The app helps local farmers schedule and monitor watering remotely, promoting sustainable agriculture in Sumatra.


## Features


# Folder Structure:
This codebase follows a monolithic architecture with front-end and back-end on separated folders to allow the team to work asynchronously and more efficiently. 


```commandline
├───frontend
└───backend
    ├───build
    │   ├───classes
    │   │   └───kotlin
    │   │       ├───main
    │   │       │   ├───com
    │   │       │   │   └───OIC
    │   │       │   │       ├───account
    │   │       │   │       │   └───model
    │   │       │   │       ├───models
    │   │       │   │       └───plugins
    │   │       │   └───META-INF
    │   │       └───test
    │   │           ├───com
    │   │           │   └───OIC
    │   │           └───META-INF
    │   ├───distributions
    │   ├───kotlin
    │   │   ├───compileKotlin
    │   │   │   ├───cacheable
    │   │   │   │   └───caches-jvm
    │   │   │   │       ├───inputs
    │   │   │   │       ├───jvm
    │   │   │   │       │   └───kotlin
    │   │   │   │       └───lookups
    │   │   │   ├───classpath-snapshot
    │   │   │   └───local-state
    │   │   └───compileTestKotlin
    │   │       ├───cacheable
    │   │       │   └───caches-jvm
    │   │       │       ├───inputs
    │   │       │       ├───jvm
    │   │       │       │   └───kotlin
    │   │       │       └───lookups
    │   │       ├───classpath-snapshot
    │   │       └───local-state
    │   ├───libs
    │   ├───reports
    │   │   ├───problems
    │   │   └───tests
    │   │       └───test
    │   │           ├───classes
    │   │           ├───css
    │   │           ├───js
    │   │           └───packages
    │   ├───resources
    │   │   └───main
    │   │       └───static
    │   ├───scripts
    │   ├───scriptsShadow
    │   ├───test-results
    │   │   └───test
    │   │       └───binary
    │   └───tmp
    │       ├───jar
    │       ├───shadowJar
    │       └───test
    ├───gradle
    │   └───wrapper
    └───src
        ├───main
        │   ├───kotlin
        │   │   ├───account
        │   │   │   └───model
        │   │   ├───controller
        │   │   ├───model
        │   │   └───plugins
        │   └───resources
        │       └───static
        └───test
            └───kotlin
```

## Dependencies


## Test Dependencies


## Authentication 
- Login Screen
- Logout Screen

## Water Irrigation
- Open valves
- Close valves
- Automation