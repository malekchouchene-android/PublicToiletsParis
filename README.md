# PublicToiletsParis
It is an Android application that provides a list of public toilets in Paris, with a user-friendly feature to filter toilets located near the user's current location.
# Technical presentation 
- Programming Language: Written entirely in <b>Kotlin</b> with <b>coroutines and <b>flow</b> .
- UI Framework: Built using <b>Jetpack Compose</b> with Material Design 3 for a modern, declarative, and accessible user interface.
- Dependency Injection: Managed with <b>Hilt</b> , a purpose-built DI library for Android .
- Networking: Handled by <b>Retrofit</b> and <b>OkHttp</b>, offering reliable and efficient API communication.
- Testing : with turbine , mockk and junit .
- clean architecture :
  - App Layer (Presentation):
     - Implements MVVM (Model-View-ViewModel) for managing UI state.
  State-driven UI with the ViewModel providing a single source of truth for the app's data.
  - Domain Layer:
    - Entities: Represent the core data structures that define the essential business logic and concepts of the app
    - Use Cases: Encapsulates application-specific business rules and orchestrates the flow of data. serving as intermediaries between the App Layer (e.g., ViewModel) and the Data Layer (e.g., repositories).
    - Repository Interfaces: Abstracts data sources to enable flexibility and easier testing.
  - Data Layer
    - Repository Implementations: Contains the logic for fetching and caching data from different sources.
    - API Integration: Uses Retrofit for RESTful communication and OkHttp for advanced networking capabilities.
- This architecture provides a clear separation of concerns across its layers, ensuring each layer has a distinct responsibility. This promotes clean, modular, and well-organized code, which significantly enhances testability and maintainability.
