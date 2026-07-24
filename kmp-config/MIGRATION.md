# Kotlin Multiplatform (KMP) & Compose Multiplatform Migration Guide

This document guides you through migrating your Android-only Jetpack Compose codebase to a **Kotlin Multiplatform (KMP)** project targeting both **Android** and **Web Applications (via Compose for Web Wasm/JS)**.

The pre-configured KMP build configurations are located in the `/kmp-config/` directory.

---

## 1. Directory Structure Mapping

To share your Kotlin and Compose UI logic across both platforms, you must transition from the standard Android structure to the KMP Source Sets layout.

### Current Structure (Android-Only)
```text
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/ (Your Kotlin UI, Models, ViewModels)
│   │   └── res/ (Android Layouts, Values, Drawables)
```

### New Structure (Kotlin Multiplatform)
```text
app/
├── src/
│   ├── commonMain/
│   │   └── kotlin/
│   │       └── com/example/
│   │           ├── model/ (Shared data models)
│   │           ├── viewmodel/ (Shared business logic ViewModels)
│   │           └── ui/
│   │               └── screens/ (Shared Jetpack Compose screens)
│   ├── androidMain/
│   │   ├── kotlin/
│   │   │   └── com/example/
│   │   │       └── MainActivity.kt (Launches the shared App composable)
│   │   └── AndroidManifest.xml
│   ├── wasmJsMain/
│   │   ├── kotlin/
│   │   │   └── com/example/
│   │   │       └── main.kt (Entry point for Wasm/JS Canvas mounting)
│   │   └── resources/
│   │       └── index.html (Web container page)
```

---

## 2. Source Code Migration Steps

### Step A: Move Code to `commonMain`
Move all files currently under `app/src/main/java/com/example/` (except `MainActivity.kt`) into **`app/src/commonMain/kotlin/com/example/`**:
- Move `model/DataModels.kt`
- Move `viewmodel/RestaurantViewModel.kt`
- Move `ui/screens/` (`AdminAnalyticsScreen.kt`, `CustomerMenuScreen.kt`, etc.)
- Move `ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`)

### Step B: Refactor Imports
Compose Multiplatform uses platform-agnostic imports for Compose UI. Ensure your shared screens use:
- `androidx.compose.runtime.*` instead of platform-specific runtime libraries.
- `androidx.compose.material3.*` (supported seamlessly across Android and Web).
- Update any image loading libraries: use multiplatform-compatible loaders (like **Coil 3** with multiplatform support, or **Kamel**) instead of `coil-compose` Android-only imports.

### Step C: Update `MainActivity.kt` (Android Entry Point)
Your Android `MainActivity.kt` should move to `app/src/androidMain/kotlin/com/example/MainActivity.kt` and act as a host that mounts the shared main layout:

```kotlin
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // MainApp is your root composable defined in commonMain
            MainApp() 
        }
    }
}
```

### Step D: Create `main.kt` (Wasm/JS Web Entry Point)
Create `app/src/wasmJsMain/kotlin/com/example/main.kt` to mount the shared Compose UI on the web browser canvas:

```kotlin
package com.example

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(title = "Krishna Chicken Web") {
        MainApp()
    }
}
```

---

## 3. Platform-Specific Implementations (`expect` / `actual`)

If you require platform-specific platform services (e.g., Toast notifications or Android's Toast APIs), define them using KMP's **expect/actual** pattern:

#### Define in `commonMain`:
```kotlin
// commonMain/kotlin/com/example/Platform.kt
expect fun showToast(message: String)
```

#### Implement in `androidMain`:
```kotlin
// androidMain/kotlin/com/example/Platform.kt
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

actual fun showToast(message: String) {
    // Access context and trigger standard Android Toast
}
```

#### Implement in `wasmJsMain`:
```kotlin
// wasmJsMain/kotlin/com/example/Platform.kt
import kotlinx.browser.window

actual fun showToast(message: String) {
    window.alert(message) // Simple web alert or custom web notification toast
}
```

---

## 4. How to Build & Run locally or on the Web

Once you have completed the file moves, copy the build configurations from `/kmp-config/` to your project root to overwrite the Android-only settings.

### Run Web App (Wasm/JS)
```bash
gradle :app:wasmJsBrowserRun
```

### Build Web Production Assets
```bash
gradle :app:wasmJsBrowserDistribution
```
The output static HTML, JS, and WebAssembly files will be generated in `app/build/dist/wasmJs/productionExecutable/` and are fully ready to be hosted on any web server (GitHub Pages, Firebase Hosting, Netlify, etc.).
