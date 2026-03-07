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

- **Language:** Kotlin
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Graphics:** OpenGL ES 3.0
- **Architecture:** MVVM with LiveData
- **Build System:** Gradle with Kotlin DSL
- **Testing:** JUnit 5, MockK, Robolectric
- **CI/CD:** GitHub Actions

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

**Triggers:**
- Push to main/master branch
- Pull requests to main/master
- Manual workflow dispatch

**Jobs:**

**1. Lint Check**
```yaml
- name: Run Kotlin Lint
  run: ./gradlew ktlintCheck
```

**2. Build**
```yaml
- name: Build Debug APK
  run: ./gradlew assembleDebug
```

**3. Unit Tests**
```yaml
- name: Run Unit Tests
  run: ./gradlew testDebugUnitTest
  
- name: Generate Coverage Report
  run: ./gradlew jacocoTestReport
```

**4. Integration Tests**
```yaml
- name: Run Integration Tests
  run: ./gradlew connectedDebugAndroidTest
  # Note: Requires emulator or uses Robolectric
```

**5. Upload Artifacts**
```yaml
- name: Upload Test Reports
  uses: actions/upload-artifact@v3
  with:
    name: test-reports
    path: app/build/reports/
    
- name: Upload Coverage Report
  uses: actions/upload-artifact@v3
  with:
    name: coverage-report
    path: app/build/reports/jacoco/
```

**6. Coverage Check**
```yaml
- name: Check Coverage Threshold
  run: |
    ./gradlew jacocoTestCoverageVerification
  # Fails if coverage < 80%
```

### Build Configuration

**build.gradle.kts additions:**

```kotlin
plugins {
    id("jacoco")
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
}
```

### Lint Configuration

**ktlint rules:**
- Standard Kotlin style guide
- Android-specific rules
- Max line length: 120 characters
- No wildcard imports
- Consistent indentation

### Pre-commit Hooks (Optional)

```bash
#!/bin/bash
# .git/hooks/pre-commit
./gradlew ktlintCheck
if [ $? -ne 0 ]; then
    echo "Lint check failed. Run ./gradlew ktlintFormat to fix."
    exit 1
fi
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
