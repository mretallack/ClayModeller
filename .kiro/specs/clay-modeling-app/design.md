# Clay Modeling App - Technical Design

## Architecture Overview

The app follows a Model-View-ViewModel (MVVM) architecture with a custom 3D rendering engine built on OpenGL ES.

### High-Level Components

```
┌─────────────────────────────────────────────────────────┐
│                     MainActivity                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │            ModelingViewModel                     │   │
│  │  - Model state management                        │   │
│  │  - Tool state                                    │   │
│  │  - Undo/redo history                            │   │
│  └─────────────────────────────────────────────────┘   │
│                                                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │            GLSurfaceView                         │   │
│  │  ┌──────────────────────────────────────────┐   │   │
│  │  │        ModelRenderer                      │   │   │
│  │  │  - OpenGL ES rendering                    │   │   │
│  │  │  - Camera management                      │   │   │
│  │  │  - Touch event handling                   │   │   │
│  │  └──────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────┘   │
│                                                          │
│  ┌─────────────────────────────────────────────────┐   │
│  │            Tool Toolbar (UI)                     │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
        ┌─────────────────────────────────────┐
        │         Core Components              │
        │                                      │
        │  ┌────────────────────────────────┐ │
        │  │      ClayModel                 │ │
        │  │  - Mesh data (vertices/faces)  │ │
        │  │  - Mesh manipulation methods   │ │
        │  └────────────────────────────────┘ │
        │                                      │
        │  ┌────────────────────────────────┐ │
        │  │      ToolEngine                │ │
        │  │  - RemoveClayTool              │ │
        │  │  - AddClayTool                 │ │
        │  │  - PullClayTool                │ │
        │  └────────────────────────────────┘ │
        │                                      │
        │  ┌────────────────────────────────┐ │
        │  │      FileManager               │ │
        │  │  - Save/load model data        │ │
        │  │  - STL export                  │ │
        │  └────────────────────────────────┘ │
        └─────────────────────────────────────┘
```

## Core Components

### 1. ClayModel

Represents the 3D mesh data and provides manipulation methods.

**Data Structure:**
- Vertices: List of 3D points (x, y, z)
- Faces: Triangles defined by vertex indices
- Normals: Surface normals for lighting

**Key Methods:**
- `initialize()`: Create initial sphere mesh
- `applyTool(tool, position, strength, radius)`: Modify mesh based on tool
- `getVertices()`: Return vertex data for rendering
- `getFaces()`: Return face indices
- `clone()`: Deep copy for undo/redo

**Mesh Representation:**
```
Sphere subdivision using icosphere algorithm:
- Start with icosahedron (20 faces)
- Subdivide each triangle recursively
- Project vertices to sphere surface
- Default: 3 subdivisions = ~1,280 faces
```

### 2. ToolEngine

Abstract tool system with concrete implementations.

**Base Tool Interface:**
```kotlin
interface Tool {
    fun apply(model: ClayModel, hitPoint: Vector3, strength: Float, radius: Float)
    fun getIcon(): Int
    fun getName(): String
}
```

**Tool Implementations:**

**RemoveClayTool:**
- Find vertices within radius of hit point
- Move vertices inward along surface normal
- Distance = strength * falloff(distance from center)
- Falloff: smooth curve (1 - (d/r)²)

**AddClayTool:**
- Find vertices within radius of hit point
- Move vertices outward along surface normal
- Same falloff as remove tool

**PullClayTool:**
- Find vertices within radius of hit point
- Move vertices in direction of drag vector
- Blend drag direction with surface normal
- Maintains surface smoothness

### 3. ModelRenderer

OpenGL ES 3.0 renderer for 3D visualization.

**Rendering Pipeline:**
1. Clear color and depth buffers
2. Set up view and projection matrices
3. Bind shader program
4. Upload vertex data to GPU
5. Upload normal data for lighting
6. Draw triangles
7. Swap buffers

**Shaders:**

**Vertex Shader:**
- Transform vertices by MVP matrix
- Pass normals to fragment shader
- Calculate lighting per-vertex

**Fragment Shader:**
- Phong lighting model
- Single directional light
- Clay-like material (matte finish)
- Color: Terracotta/clay tone

**Camera System:**
- Orbit camera around model center
- Touch rotation: Convert screen delta to rotation angles
- Pinch zoom: Adjust camera distance
- Pan: Move camera target position

### 4. FileManager

Handles model persistence and export.

**Save Format (Custom Binary):**
```
Header:
- Magic number: "CLAY" (4 bytes)
- Version: 1 (4 bytes)
- Vertex count (4 bytes)
- Face count (4 bytes)

Data:
- Vertices: [x, y, z] * count (float32)
- Faces: [v1, v2, v3] * count (int32)
```

**STL Export:**
- Binary STL format
- 80-byte header
- Triangle count (4 bytes)
- For each triangle:
  - Normal vector (12 bytes)
  - 3 vertices (36 bytes)
  - Attribute byte count (2 bytes)

**Storage Locations:**
- Internal storage: `/data/data/com.claymodeler/files/models/`
- STL exports: `/storage/emulated/0/Download/`

### 5. ModelingViewModel

Manages app state and business logic.

**State:**
- Current model: ClayModel
- Active tool: Tool
- Tool settings: brush size, strength
- Undo stack: List<ClayModel>
- Redo stack: List<ClayModel>

**Methods:**
- `applyTool(position, strength)`: Apply current tool and save to undo stack
- `undo()`: Restore previous model state
- `redo()`: Reapply undone action
- `saveModel(name)`: Persist model to storage
- `loadModel(name)`: Load model from storage
- `exportSTL(name)`: Export as STL file

## Touch Interaction Flow

```
User Touch Event
      │
      ▼
GLSurfaceView.onTouchEvent()
      │
      ├─── Single finger drag ──→ Rotate camera
      │
      ├─── Pinch gesture ──→ Zoom camera
      │
      ├─── Two finger drag ──→ Pan camera
      │
      └─── Touch on model ──→ Ray casting
                                    │
                                    ▼
                              Find hit point on mesh
                                    │
                                    ▼
                              ViewModel.applyTool()
                                    │
                                    ▼
                              Tool.apply(model, hitPoint, ...)
                                    │
                                    ▼
                              Model mesh updated
                                    │
                                    ▼
                              Renderer draws updated mesh
```

## Ray Casting Algorithm

To determine where the user touched on the 3D model:

1. Convert screen coordinates to normalized device coordinates
2. Unproject to create ray in world space
3. Test ray against all triangles in mesh
4. Find closest intersection point
5. Return hit point and surface normal

## Performance Optimizations

**Spatial Partitioning:**
- Octree structure for fast vertex lookup
- Only test nearby vertices when applying tools
- Rebuild octree after significant changes

**GPU Optimization:**
- Use Vertex Buffer Objects (VBO)
- Update only modified vertex regions
- Batch draw calls

**Undo/Redo:**
- Limit history to 20 actions
- Store deltas instead of full copies (future optimization)

**Mesh Simplification:**
- Optional: Reduce triangle count for export
- Preserve visual quality while reducing file size

## UI Layout

```
┌─────────────────────────────────────────┐
│  [≡] Clay Modeler        [↶] [↷] [💾]  │ ← Top bar
├─────────────────────────────────────────┤
│                                         │
│                                         │
│           3D Viewport                   │
│         (GLSurfaceView)                 │
│                                         │
│                                         │
├─────────────────────────────────────────┤
│  [Remove] [Add] [Pull]                  │ ← Tool bar
├─────────────────────────────────────────┤
│  Size:   [────●────]                    │ ← Settings
│  Strength: [──●──────]                  │
└─────────────────────────────────────────┘
```

## Technology Stack

- **Language:** Kotlin 2.2.20
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 36 (Android 14)
- **Compile SDK:** 36
- **Graphics:** OpenGL ES 3.0
- **Architecture:** MVVM with LiveData
- **Build System:** Gradle 8.13.0 with Kotlin DSL
- **Java Version:** 21 (OpenJDK)
- **Testing:** Kotest (JUnit 5), MockK, Robolectric
- **CI/CD:** GitHub Actions

## Development Environment

**Local Setup:**
- Android SDK: `/home/mark/android-sdk/`
- Java: OpenJDK 21.0.10
- Gradle: Wrapper-based (./gradlew)
- Build tools, platforms, and licenses configured

**Project Structure:**
- Root: `/home/mark/git/ClayModeler/`
- Multi-module support (app module primary)
- Gradle Kotlin DSL for all build files

## Dependencies

```kotlin
dependencies {
    // Android core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // OpenGL utilities
    implementation("androidx.opengl:opengl:1.0.0")
    
    // File I/O
    implementation("androidx.documentfile:documentfile:1.0.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
}
```

## Testing Strategy

### Unit Tests

**Test Coverage Targets:**
- ClayModel: 90%+ coverage
- Tool implementations: 90%+ coverage
- FileManager: 85%+ coverage
- ModelingViewModel: 85%+ coverage
- Overall business logic: 80%+ coverage

**ClayModel Tests:**
```kotlin
class ClayModelTest {
    @Test fun `initialize creates sphere with correct vertex count`()
    @Test fun `applyTool modifies vertices within radius`()
    @Test fun `applyTool does not modify vertices outside radius`()
    @Test fun `clone creates independent copy`()
    @Test fun `getVertices returns correct data format`()
}
```

**Tool Tests:**
```kotlin
class RemoveClayToolTest {
    @Test fun `apply moves vertices inward`()
    @Test fun `apply respects strength parameter`()
    @Test fun `apply uses smooth falloff`()
}

class AddClayToolTest {
    @Test fun `apply moves vertices outward`()
    @Test fun `apply blends smoothly with surface`()
}

class PullClayToolTest {
    @Test fun `apply moves vertices in drag direction`()
    @Test fun `apply maintains surface smoothness`()
}
```

**FileManager Tests:**
```kotlin
class FileManagerTest {
    @Test fun `saveModel writes valid file format`()
    @Test fun `loadModel reads saved file correctly`()
    @Test fun `loadModel handles corrupted files`()
    @Test fun `exportSTL creates valid STL format`()
    @Test fun `exportSTL handles empty model`()
}
```

**ViewModel Tests:**
```kotlin
class ModelingViewModelTest {
    @Test fun `applyTool updates model state`()
    @Test fun `applyTool adds to undo stack`()
    @Test fun `undo restores previous state`()
    @Test fun `redo reapplies undone action`()
    @Test fun `undo stack limited to 20 items`()
    @Test fun `saveModel persists current state`()
    @Test fun `loadModel updates current state`()
}
```

### Integration Tests

**Workflow Tests:**
```kotlin
class ModelWorkflowTest {
    @Test fun `save and load preserves model data`()
    @Test fun `export STL creates valid file`()
    @Test fun `multiple tool applications work correctly`()
    @Test fun `undo redo cycle maintains consistency`()
}
```

**Test Execution:**
- Unit tests: Run on JVM using Robolectric (no emulator needed)
- Integration tests: Run on JVM where possible, emulator for UI tests
- Mock OpenGL calls for renderer tests

### Test Utilities

**Mock Data:**
```kotlin
object TestData {
    fun createSimpleSphere(subdivisions: Int = 1): ClayModel
    fun createTestModel(vertexCount: Int): ClayModel
    fun createMockTouchEvent(x: Float, y: Float): MotionEvent
}
```

**Assertions:**
```kotlin
fun assertVertexNear(expected: Vector3, actual: Vector3, tolerance: Float)
fun assertMeshValid(model: ClayModel)
fun assertFileFormatValid(file: File)
```

## CI/CD Pipeline

### GitHub Actions Workflow

**File:** `.github/workflows/android-ci.yml`

**Environment:**
- Runner: `ubuntu-latest`
- Java: OpenJDK 21 (Temurin distribution)
- Gradle cache enabled

**Triggers:**
- Push to main/master branch
- Pull requests to main/master
- Manual workflow dispatch
- Ignore paths: README.md, docs, meta files

**Jobs:**

**1. Validate Gradle Wrapper**
```yaml
- name: Validate Gradle wrapper
  uses: gradle/actions/wrapper-validation@v5
```

**2. Setup Environment**
```yaml
- name: Check out repository
  uses: actions/checkout@v4
  
- name: Set up Java JDK
  uses: actions/setup-java@v4
  with:
    java-version: 21
    distribution: "temurin"
    cache: 'gradle'
```

**3. Build and Test**
```yaml
- name: Build and test
  run: ./gradlew assembleDebug lintDebug testDebugUnitTest --stacktrace
```

**4. Upload Artifacts**
```yaml
- name: Upload APK
  uses: actions/upload-artifact@v6
  with:
    name: app
    path: app/build/outputs/apk/debug/*.apk
    
- name: Upload Test Reports
  if: always()
  uses: actions/upload-artifact@v6
  with:
    name: test-reports
    path: app/build/reports/tests/
    
- name: Upload Lint Reports
  if: always()
  uses: actions/upload-artifact@v6
  with:
    name: lint-reports
    path: app/build/reports/lint/
```

**5. Coverage (Optional)**
```yaml
- name: Generate Coverage Report
  run: ./gradlew jacocoTestReport
  
- name: Upload Coverage
  uses: actions/upload-artifact@v6
  with:
    name: coverage-report
    path: app/build/reports/jacoco/
```

### Build Configuration

**build.gradle.kts (Project level):**

```kotlin
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
}
```

**build.gradle.kts (App module):**

```kotlin
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("jacoco")
}

android {
    namespace = "com.claymodeler"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.claymodeler"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        viewBinding = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    classDirectories.setFrom(files("build/tmp/kotlin-classes/debug"))
    executionData.setFrom(files("build/jacoco/testDebugUnitTest.exec"))
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")
    
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

**settings.gradle.kts:**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ClayModeler"
include(":app")
```

**gradle/libs.versions.toml:**

```toml
[versions]
kotlin = "2.2.20"
gradle = "8.13.0"
appcompat = "1.7.1"
lifecycle = "2.7.0"
opengl = "1.0.0"
documentfile = "1.0.1"
kotest = "6.0.3"
mockk = "1.13.8"
robolectric = "4.11.1"
androidxTest = "1.7.0"
espresso = "3.5.1"
coroutines = "1.7.3"
jacoco = "0.8.11"
minSdk = "26"
targetSdk = "36"
compileSdk = "36"
java = "21"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.12.0" }
androidx-appcompat = { module = "androidx.appcompat:appcompat", version.ref = "appcompat" }
material = { module = "com.google.android.material:material", version = "1.11.0" }
lifecycle-viewmodel-ktx = { module = "androidx.lifecycle:lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
lifecycle-livedata-ktx = { module = "androidx.lifecycle:lifecycle-livedata-ktx", version.ref = "lifecycle" }
opengl = { module = "androidx.opengl:opengl", version.ref = "opengl" }
documentfile = { module = "androidx.documentfile:documentfile", version.ref = "documentfile" }

# Testing
kotest-runner-junit5 = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }
kotest-assertions-core = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }
kotest-property = { module = "io.kotest:kotest-property", version.ref = "kotest" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
robolectric = { module = "org.robolectric:robolectric", version.ref = "robolectric" }
androidx-test-core = { module = "androidx.test:core", version.ref = "androidxTest" }
androidx-test-ext-junit = { module = "androidx.test.ext:junit", version = "1.1.5" }
androidx-test-runner = { module = "androidx.test:runner", version.ref = "androidxTest" }
androidx-test-rules = { module = "androidx.test:rules", version.ref = "androidxTest" }
androidx-test-espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }

[plugins]
com-android-application = { id = "com.android.application", version.ref = "gradle" }
org-jetbrains-kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

**local.properties:**

```properties
sdk.dir=/home/mark/android-sdk
```

## Test Data Management

**Test Models:**
- Simple sphere (100 vertices) for fast tests
- Medium sphere (1000 vertices) for integration tests
- Complex model (10000 vertices) for performance tests

**Test Files:**
- Valid save files for load testing
- Corrupted files for error handling tests
- STL reference files for export validation

## Error Handling

**Rendering Errors:**
- Catch OpenGL errors after each operation
- Log errors and display user-friendly message
- Fallback to safe state (reload last valid model)

**File I/O Errors:**
- Validate file format before loading
- Handle corrupted files gracefully
- Display specific error messages (file not found, permission denied, etc.)

**Memory Errors:**
- Monitor mesh complexity
- Warn user if model becomes too complex
- Implement mesh decimation if needed

**Touch Input Errors:**
- Validate ray casting results
- Handle edge cases (touch outside model)
- Prevent tool application if no valid hit point

## Future Enhancements

- Additional tools: Smooth, Flatten, Pinch
- Symmetry mode for mirrored editing
- Multiple clay colors/materials
- Texture painting
- Reference image overlay
- Cloud save/sync
- Model sharing
