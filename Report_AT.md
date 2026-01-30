# App Notes: Architecture & Technical Choices - Anh Tuan M.

This document explains the main decisions made for the app's architecture, tools, and future plans.

## Architecture

-   **Architectural Style**: The project is based on a style called "First Feature Clean Architecture" (FFCA). It's a variation of the standard Clean Architecture, modified to better fit the needs of Android apps. Some parts of the app are designed to be portable and could be reused in other projects, including Kotlin Multiplatform (KMP) apps.
    -   Each feature is organized into layers: Domain (for business rules), Data (for handling data), and Presentation/UI (for the user interface).
    -   The UI layer uses the MVVM (Model-View-ViewModel) pattern. It leverages modern Kotlin features like Coroutine Flows and UIState classes to handle data reactively. ViewModels are designed to survive configuration changes (like screen rotation), which significantly improves the user experience by maintaining a smooth and consistent UI.

-   **Architectural Choice**: While a traditional Clean Architecture is well-suited for small to medium-sized applications, I chose FFCA to showcase how I would approach building a large-scale application. This style promotes growth and effective team collaboration.

-   **Dependency Management**: The project uses Hilt for dependency injection. This makes the code more organized and easier to test, and it is an improvement over the previous setup, which resembled a Service Locator pattern.

## Libraries & Tools

-   **Android Jetpack**: The project heavily utilizes Jetpack components, including Room for the database, ViewModel for state management, and Navigation for handling screen flows.
-   **Coil**: Used for loading and caching images efficiently.
-   **Spark**: The design system from Adevinta, used for UI components to ensure a consistent look and feel.
-   **Timber**: Used for application logging.

## Testing Strategy

-   **JUnit4**: I use JUnit4 for unit tests because it's a common choice and works well with other Android testing tools. While JUnit5 is newer, it can be tricky to set up with some tools (KSP, Robolectric), so I chose the more stable and widely-supported JUnit4 for now.

-   **Robolectric**: Used to run Android-specific unit tests on the local JVM, which removes the need to run them on an emulator or physical device.

-   **MockK**: This tool is made for Kotlin and helps me write shorter and clearer tests.

-   **Turbine**: I use this to test Kotlin Flows, which provides an easy way to check the data streams in the app.

## Implementation & Fixes

-   **Splash Screen**: A splash screen is the first screen the user sees, which improves the user experience, especially on slower devices where the app may take a moment to load.

-   **`DetailsActivity` Replacement**: The unnecessary `DetailsActivity` was replaced with a composable `SongDetailsScreen`. Navigation to this screen from external apps or deep links can be handled through the app's single `MainActivity` and `NavHost`.

-   **ViewModel Refinements**: The `AlbumsViewModel` was initially using `GlobalScope`, which can cause memory leaks. This was fixed by switching to the safer, lifecycle-aware `viewModelScope`. The ViewModel's state management was also improved by using `StateFlow` with a dedicated `UIState` class to smoothly handle configuration changes, such as screen rotation.

-   **Album List Paging**: The server sends a list of almost 5,000 songs. To avoid performance issues and a poor user experience, the data is displayed in pages of 30 items. This is an area for future improvement.

-   **Saving Favorites**: Favorite songs are saved in a dedicated table using the Room database. This ensures favorites are kept even if a song is removed from the server and simplifies data management when the main song list changes. Users can toggle a song's favorite status on both the main list and the details screen.

-   **Data Models**: The app uses distinct models for different layers to ensure good separation of concerns. For example, `SongDto` is used for network data, `SongEntity` for the database, and `Song` as the domain model for use within the app's UI and business logic.

-   **Image Caching**: The app provides two image caching strategies to support offline mode: a large 100 MB cache for long-term storage and another for images that are updated more often.

-   **`DataModule` Logging Fix**: Logging was incorrectly enabled in `RELEASE` builds and disabled in `DEBUG` builds. This has been corrected.

-   **`AnalyticsHelper` Injection**: The `AnalyticsHelper` was updated to have its `Context` dependency injected directly, which improves testability.

-   **`AlbumItem` UI Fix**: The `modifier` parameter in the `AlbumItem` composable was corrected to ensure proper layout behavior.

## Future Plans

### Important Next Steps

-   **Crash Reporting**: Add Firebase Crashlytics to find and fix crashes.
-   **App Size and Security**: Use R8/Proguard to make the app smaller and more secure before release.
-   **UI Testing**: Add automated tests for the UI to ensure quality and prevent regressions (Espresso).
-   **Test Coverage**: Check and improve how much of the code is covered by tests to maintain code quality.
-   **Analytics SDK**: Integrate a proper analytics SDK (Firebase Analytics) to track user behavior and app usage based on product requirements.

### On-Demand Features & Improvements

-   **Refine ViewModel Dependencies**: To more strictly follow Clean Architecture, the `AlbumsViewModel` could be refactored to only depend on Use Cases, not directly on the `AlbumRepository`. This choice can depend on team coding conventions.
-   **UI Enhancements**: Add a top app bar or bottom navigation bar for better navigation and a more polished look.
-   **Favorites Screen**: Create a dedicated screen to list all songs the user has marked as a favorite.
-   **Error Messages**: Implement a more robust system to show user-friendly error messages.
-   **Better Paging**: Enhance the paging system by having the server send songs in pages. When offline, retrieve songs in manageable chunks instead of loading all of them.
-   **Album Detail Screen**: Implement a new screen that shows all songs belonging to a specific album when an album is tapped.
-   **More Languages**: Add support for different languages based on the target audience.
-   **Delete Old Images**: Create a system to automatically delete old saved images after a certain period (e.g., 7 days) to manage storage and ensure displayed images are fresh.