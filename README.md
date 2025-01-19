# PublicToiletsParis
It is an Android application that provides a list of public toilets in Paris, with a user-friendly feature to filter toilets located near the user's current location.
# Technical presentation 
- Programming Language: Written entirely in Kotlin.
- UI Framework: Built using Jetpack Compose with Material Design 3 for a modern, declarative, and accessible user interface.
- Dependency Injection: Managed with Hilt suitable to android .
- Networking: Handled by Retrofit and OkHttp, offering reliable and efficient API communication.
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
 
