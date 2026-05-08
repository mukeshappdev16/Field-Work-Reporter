# Field Work Reporter

Field Work Reporter is a modern Android application designed for field professionals to capture and organize task-related data efficiently. It allows users to create tasks and attach rich media including photos, voice recordings, and text notes, all within a clean and intuitive interface.

## Key Features

- **Task Management**: Create and track tasks with titles and descriptions.
- **Rich Media Attachments**:
    - **Photos**: Capture field images directly using the camera.
    - **Voice Notes**: Record audio feedback or site observations.
    - **Text Notes**: Write detailed notes for each task.
- **Task Lifecycle**: Support for "Draft" and "Completed" states. Completed tasks are locked to prevent accidental updates.
- **Offline First**: All data is stored locally using Room database, ensuring functionality without an internet connection.
- **Sync Ready**: Includes a dedicated sync action to prepare data for cloud integration.
- **Type-Safe Navigation**: Modern navigation flow using Kotlinx Serialization.
- **Professional UI**: Clean, eye-catchy Material 3 design with a professional Slate & Indigo color palette.

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Declarative UI)
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Database**: [Room](https://developer.android.com/training/data-storage/room) (Multi-table relational schema)
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/guide/navigation/navigation-getting-started) (Type-safe routes)
- **Media Handling**:
    - **CameraX / Activity Result API**: For photo capture.
    - **MediaRecorder / MediaPlayer**: For voice note management.
- **Image Loading**: [Coil 3](https://coil-kt.github.io/coil/)
- **Serialization**: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- **Concurrency**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)

## Project Structure

- **`data`**: Room entities, DAOs, and the Repository implementation.
- **`domain`**: Domain models (clean POJOs used in the UI layer).
- **`presentation`**:
    - **`home`**: Task list and creation dialog.
    - **`detail`**: Rich media capture and task completion logic.
    - **`ui.theme`**: Custom Material 3 theme and color system.
- **`util`**: File management and URI utilities.

## Getting Started

1. Clone the repository.
2. Open in Android Studio (Ladybug or newer recommended).
3. Sync Gradle and Run on an emulator or physical device (API 26+).
