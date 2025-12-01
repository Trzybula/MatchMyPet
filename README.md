This is a Kotlin Multiplatform project targeting Web, Server.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.

* [/server](./server/src/main/kotlin) is for the Ktor server application.

* [/shared](./shared/src) is for the code that will be shared between all targets in the project.

### Build and Run Server

To build and run the development version of the server, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :server:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :server:run
  ```

### Build and Run Web Application

- for the JS target (slower, supports older browsers):
  - on macOS/Linux
    ```shell
    ./gradlew :composeApp:jsBrowserDevelopmentRun
    ```
  - on Windows
    ```shell
    .\gradlew.bat :composeApp:jsBrowserDevelopmentRun
    ```

---
### MatchMyPet
is a small full-stack Kotlin Multiplatform project:
a simple web dashboard for animal shelters to register, log in, and manage their own pets.

---
### Features:
- Shelter registration & login
- Each shelter sees only its own pets
- Add new pets (name, species, age, description, etc.)
- View pet list filtered by shelterId
- Automatic SQLite schema creation via SQLDelight

--- 
### TODO/SECOND ITERATION:

#### For adopters (second iteration):
- Pet search & filtering (species, size, age, availability)
- Pet detail page with photo gallery
- “Send adoption request” form
- Shelter inbox for incoming adoption requests

#### Improvements for shelters:
- Edit pet info
- Upload & manage pet photos
- Toggle availability (adopted / reserved / available)
- Better error handling and form validations