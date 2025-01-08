
# Kotlin Library to be compatible for multiple platform

Date: 2025-01-07

## Status

Approved - **Approach 2(Using Kotlin Multi Platform(KMP))**

## Consequences of selected approach

- Maintaining single InjiVcRenderer library(KMP Project) with platform specific code only for the necessary implementation like qrData to base64 conversion with rest of the logic shared across platforms.
- We are targeting Android and Java Platform in our KMP project, so that it can generate artifacts which can be used in Mimoto(Java based implementation) and Inji(Android implementation)


## Context

In InjiVcRenderer Library, we are converting SVG Template into SVG Image by replacing the placeholders. We are also replacing the qrCodeImage placeholder with the base64 Qr code image generated from PixelPass. Pixel gives qrData and we have to convert qrData into base64 string.

For these conversion, Android needs **androidx.graphics.Bitmap** and Java needs **java.awt.BufferedImage**. We are looking for the design to build the library which is compatible for both the platforms.

## Proposed Approaches

### Approach 1 (Creating two seperate Libraries)

Creating seperate libraries for Android and Java which generates **jar** and **aar** respectively with platform specific implementations. So that, jar can be used in any Java applications in which this Library needs to be integrated and aar can be used in any Android application.


#### Pros

- **Tailored Functionality**: By having separate libraries, we can optimize each version for the specific platform’s needs and constraints.
- **No Cross-Platform Dependency Conflicts**: Android projects and Java SE projects typically use different sets of dependencies. By having two libraries, you ensure that each platform only pulls in the dependencies it needs.
- **Cleaner Build Configurations**: The Gradle or Maven configurations for each platform can remain clean and simpler. The Android build process doesn’t need to be concerned with Java SE dependencies, and vice versa, minimizing the complexity of your build system.


#### Cons
- **Dual Codebases**: Maintaining two separate libraries means we need to keep track of changes and updates in both codebases. Any new feature or bug fix in the core library must be duplicated in both libraries, which can become cumbersome, especially as the number of platforms (Android, Java SE) increases.
- **Duplicate Effort**: For every new feature, bug fix, or enhancement, we need to implement and test the feature in both libraries, even though the underlying functionality may be the same. This can slow down the development process as you need to adjust and optimize the code for each platform separately.
- **Version Divergence**: Over time, the two libraries may diverge in terms of features, bug fixes, and API changes. If we don't actively ensure that both versions are in sync, it may result in inconsistencies between the Android and Java SE versions of the library.
- **Limited Shared Cod**e: When maintaining separate libraries, it can be harder to extract common shared functionality into a core module or class, especially if the Android and Java SE APIs differ significantly. This can lead to duplication of logic or missed opportunities for code reuse.


### Approach 2(Using Kotlin Multi Platform(KMP))

KMP (Kotlin Multiplatform) is a feature of Kotlin that allows you to write shared code that runs on multiple platforms. It enables the use of a single codebase across various environments, like Android, iOS, JavaScript, and JVM (Java Virtual Machine), by letting developers write platform-specific code while still allowing for platform-specific implementations wherever needed.

#### Pros

- **Platform-Specific Customization** : We can write platform-specific code for when you need to interact with platform-specific APIs or features, but still maintain a large portion of shared code.
- **Code Sharing Across Platforms**: You can share code across multiple platforms, reducing the need to rewrite the same logic for each platform. This results in a reduced development effort and faster time to market.
- **Gradle Support**:Kotlin Multiplatform is fully supported by Gradle, which makes it easier to manage dependencies and configure project setups for different platforms.
- **Mutliple Target Platforms** : KMP allows us to target multiple platforms (e.g., mobile and web, or mobile and backend) and not just limited to Mobile.
- **Simplified Dependency Management**:
    - **Shared Dependencies**: Dependencies in KMP can be shared across platforms, which simplifies the process of managing them. The shared code can depend on common libraries or Kotlin-based libraries (e.g., Ktor for networking, Kotlin serialization), and those dependencies will be used across all platforms.
    - **Platform-Specific Dependencies**: We can also define platform-specific dependencies (e.g., Android-specific libraries in androidMain, iOS-specific libraries in iosMain) without worrying about version conflicts, as each platform module is isolated.

#### Cons

- **No Shared UI Code**: One of the biggest drawbacks of KMP is that UI code cannot be shared across platforms. While you can share business logic, models, and other backend-related code, each platform requires its own UI implementation (e.g., using Jetpack Compose for Android and SwiftUI for iOS).
- **Steep Learning Curve**: While KMP lets you share business logic, the platform-specific parts of the code still require knowledge of native development for Android (Kotlin/Java), iOS (Swift/Objective-C), or other platforms.
- **Smaller Community Compared to React Native/Flutter**: While Kotlin is a popular language, Kotlin Multiplatform is relatively new compared to established frameworks like React Native and Flutter. 
