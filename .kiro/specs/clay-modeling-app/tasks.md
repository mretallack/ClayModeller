# Clay Modeling App - Implementation Tasks

## Phase 1: Project Setup & Foundation (Week 1)

### Task 1.1: Project Initialization
- [ ] Create Android project structure with Gradle Kotlin DSL
- [ ] Set up `settings.gradle.kts` with repository configuration
- [ ] Create `gradle/libs.versions.toml` with all dependencies
- [ ] Configure `build.gradle.kts` (project and app module)
- [ ] Set up `local.properties` with SDK path
- [ ] Create `.gitignore` for Android project
- [ ] Verify project builds successfully

**Expected Outcome:** Empty Android project that compiles

### Task 1.2: Core Data Structures
- [ ] Create `Vector3` class (x, y, z with math operations)
- [ ] Create `Face` class (3 vertex indices)
- [ ] Create `ClayModel` class with vertices, faces, normals lists
- [ ] Implement `ClayModel.initialize()` for icosphere generation
- [ ] Add unit tests for Vector3 math operations
- [ ] Add unit tests for icosphere generation

**Expected Outcome:** ClayModel can generate initial sphere mesh

### Task 1.3: Basic UI Layout
- [ ] Create `MainActivity` with MVVM structure
- [ ] Create `ModelingViewModel` with LiveData
- [ ] Design XML layout with GLSurfaceView and tool buttons
- [ ] Implement portrait layout
- [ ] Implement landscape layout (tools on side)
- [ ] Add Material Design theme and colors
- [ ] Test orientation changes preserve state

**Expected Outcome:** UI layout displays correctly in both orientations

## Phase 2: 3D Rendering (Week 2)

### Task 2.1: OpenGL Renderer Setup
- [ ] Create `ModelRenderer` implementing GLSurfaceView.Renderer
- [ ] Implement `onSurfaceCreated()` with OpenGL initialization
- [ ] Implement `onSurfaceChanged()` with viewport setup
- [ ] Create vertex and fragment shaders (Phong lighting)
- [ ] Compile and link shader program
- [ ] Set up projection and view matrices
- [ ] Add error handling for OpenGL operations

**Expected Outcome:** Black screen with OpenGL context initialized

### Task 2.2: Mesh Rendering
- [ ] Create Vertex Buffer Objects (VBO) for vertices
- [ ] Create VBO for normals
- [ ] Create Element Buffer Object (EBO) for faces
- [ ] Implement `onDrawFrame()` to render mesh
- [ ] Add terracotta clay color and lighting
- [ ] Test rendering with default sphere
- [ ] Verify 30+ FPS performance

**Expected Outcome:** Clay sphere renders with proper lighting

### Task 2.3: Camera Controls
- [ ] Implement camera orbit around model center
- [ ] Add pinch-to-zoom gesture detection
- [ ] Add single-finger drag for rotation
- [ ] Add two-finger drag for pan
- [ ] Implement camera matrix calculations
- [ ] Add smooth interpolation for gestures
- [ ] Test camera controls feel natural

**Expected Outcome:** User can view model from all angles

## Phase 3: Tool System (Week 3)

### Task 3.1: Tool Architecture
- [ ] Create `Tool` interface with `apply()` method
- [ ] Create `ToolEngine` to manage active tool
- [ ] Implement tool selection in ViewModel
- [ ] Add tool state to UI (highlight selected tool)
- [ ] Create tool cursor preview overlay
- [ ] Test tool switching

**Expected Outcome:** Tool selection system works

### Task 3.2: Ray Casting
- [ ] Implement screen-to-world ray calculation
- [ ] Create octree for spatial acceleration
- [ ] Implement ray-triangle intersection
- [ ] Find closest hit point on mesh
- [ ] Return hit point and surface normal
- [ ] Add unit tests for ray casting
- [ ] Optimize for performance

**Expected Outcome:** Touch input accurately hits mesh surface

### Task 3.3: Remove Clay Tool
- [ ] Implement `RemoveClayTool` class
- [ ] Find vertices within brush radius
- [ ] Calculate falloff curve (1 - (d/r)²)
- [ ] Move vertices inward along normals
- [ ] Update VBOs with modified vertices
- [ ] Recalculate normals for affected area
- [ ] Test tool feels responsive
- [ ] Add unit tests for vertex modification

**Expected Outcome:** User can carve clay from sphere

### Task 3.4: Add Clay Tool
- [ ] Implement `AddClayTool` class
- [ ] Move vertices outward along normals
- [ ] Use same falloff as Remove tool
- [ ] Smooth blending with existing surface
- [ ] Test tool builds up clay naturally
- [ ] Add unit tests

**Expected Outcome:** User can add clay to model

### Task 3.5: Pull Clay Tool
- [ ] Implement `PullClayTool` class
- [ ] Calculate drag direction from touch movement
- [ ] Blend drag direction with surface normal
- [ ] Move vertices along blended direction
- [ ] Maintain surface smoothness
- [ ] Test tool creates natural protrusions
- [ ] Add unit tests

**Expected Outcome:** User can pull clay outward

### Task 3.6: View Mode Tool
- [ ] Implement `ViewModeTool` class
- [ ] Disable mesh modification when active
- [ ] Enable all camera gestures
- [ ] Add zoom level indicator UI
- [ ] Add reset camera button (FAB)
- [ ] Test all gestures work in view mode
- [ ] Ensure pivot point is model center

**Expected Outcome:** User can examine model without editing

## Phase 4: Tool Settings (Week 4)

### Task 4.1: Brush Size Control
- [ ] Add slider UI for brush size
- [ ] Connect slider to ViewModel
- [ ] Update tool cursor size in real-time
- [ ] Apply size to tool calculations
- [ ] Add haptic feedback on change
- [ ] Test range (min to max)

**Expected Outcome:** Brush size adjusts tool effect area

### Task 4.2: Strength Control
- [ ] Add slider UI for strength
- [ ] Connect slider to ViewModel
- [ ] Apply strength multiplier to tools
- [ ] Test subtle vs dramatic changes
- [ ] Ensure smooth gradation

**Expected Outcome:** Strength controls how much clay moves

## Phase 5: Undo/Redo (Week 4)

### Task 5.1: History Management
- [ ] Implement undo stack in ViewModel (max 20)
- [ ] Implement redo stack
- [ ] Clone model on each tool application
- [ ] Add undo/redo buttons to UI
- [ ] Enable/disable buttons based on stack state
- [ ] Add unit tests for undo/redo logic
- [ ] Test memory usage with full stack

**Expected Outcome:** User can undo/redo actions

## Phase 6: File Management (Week 5)

### Task 6.1: Clay File Format
- [ ] Implement binary file writer with header
- [ ] Write metadata as JSON
- [ ] Write vertex, face, normal data
- [ ] Calculate and write CRC32 checksum
- [ ] Implement atomic file write (temp + rename)
- [ ] Add unit tests for file format
- [ ] Test round-trip save/load

**Expected Outcome:** Models save to .clay format

### Task 6.2: File Loading
- [ ] Implement binary file reader
- [ ] Validate magic number and version
- [ ] Verify checksum
- [ ] Parse JSON metadata
- [ ] Read vertex, face, normal data
- [ ] Reconstruct ClayModel object
- [ ] Handle corrupted files gracefully
- [ ] Add unit tests for loading
- [ ] Test with various file sizes

**Expected Outcome:** Models load from .clay files

### Task 6.3: Save Dialog
- [ ] Create save dialog UI
- [ ] Add filename text input
- [ ] Validate filename (no special chars)
- [ ] Show confirmation on success
- [ ] Handle save errors (storage full, etc.)
- [ ] Test save workflow

**Expected Outcome:** User can save models with custom names

### Task 6.4: Load Dialog
- [ ] Create load dialog UI
- [ ] List saved models from storage
- [ ] Generate thumbnails (128x128 PNG)
- [ ] Show creation date for each model
- [ ] Handle empty state (no models)
- [ ] Load selected model
- [ ] Handle load errors
- [ ] Test load workflow

**Expected Outcome:** User can load previously saved models

### Task 6.5: Auto-Save
- [ ] Implement auto-save coroutine (1 minute)
- [ ] Save to autosave.clay silently
- [ ] Track last modified time
- [ ] Check for autosave on app start
- [ ] Show restore dialog if autosave exists
- [ ] Delete autosave after manual save
- [ ] Test crash recovery
- [ ] Add unit tests for auto-save logic

**Expected Outcome:** Work auto-saves every minute

## Phase 7: STL Export (Week 6)

### Task 7.1: STL Export Implementation
- [ ] Implement coordinate system conversion (Y-up to Z-up)
- [ ] Implement scale to millimeters
- [ ] Calculate face normals
- [ ] Ensure counter-clockwise winding
- [ ] Write binary STL format (little-endian)
- [ ] Add progress tracking for large models
- [ ] Save to Downloads using MediaStore API
- [ ] Add unit tests for STL format
- [ ] Test with 3D printing software

**Expected Outcome:** Models export as valid STL files

### Task 7.2: Mesh Validation
- [ ] Implement manifold check (edge counting)
- [ ] Implement degenerate triangle detection
- [ ] Check normal orientation (outward-facing)
- [ ] Skip degenerate triangles on export
- [ ] Fix inverted normals automatically
- [ ] Add unit tests for validation
- [ ] Test with non-manifold meshes

**Expected Outcome:** Export validates and repairs meshes

### Task 7.3: Export Dialog
- [ ] Create export dialog UI
- [ ] Add size options (50/100/150/200/custom mm)
- [ ] Add quality settings (Low/Medium/High)
- [ ] Add validation checkbox
- [ ] Show progress bar during export
- [ ] Display success message with file location
- [ ] Handle export errors
- [ ] Test export workflow

**Expected Outcome:** User can export with custom settings

## Phase 8: Testing & Quality (Week 7)

### Task 8.1: Unit Tests
- [ ] Write ClayModel tests (90%+ coverage)
- [ ] Write Tool tests (90%+ coverage)
- [ ] Write FileManager tests (85%+ coverage)
- [ ] Write file format validation tests (95%+ coverage)
- [ ] Write STL export tests (90%+ coverage)
- [ ] Write ViewModel tests (85%+ coverage)
- [ ] Achieve 80%+ overall coverage
- [ ] All tests pass in CI

**Expected Outcome:** Comprehensive test coverage

### Task 8.2: Integration Tests
- [ ] Test save/load workflow
- [ ] Test STL export workflow
- [ ] Test undo/redo with multiple operations
- [ ] Test auto-save and restore
- [ ] Test orientation changes
- [ ] All integration tests pass

**Expected Outcome:** Critical workflows validated

### Task 8.3: Performance Testing
- [ ] Profile rendering performance (target 30+ FPS)
- [ ] Test with 50K vertex model
- [ ] Measure memory usage
- [ ] Test on low-end device (Android 8.0, 2GB RAM)
- [ ] Optimize bottlenecks
- [ ] Verify no memory leaks

**Expected Outcome:** App performs well on target devices

### Task 8.4: Device Testing
- [ ] Test on phone (5" screen)
- [ ] Test on tablet (10" screen)
- [ ] Test on different manufacturers (Samsung, Pixel, OnePlus)
- [ ] Test on Android 8.0, 10, 12, 14
- [ ] Fix device-specific issues

**Expected Outcome:** App works across devices

## Phase 9: Polish & UX (Week 8)

### Task 9.1: Accessibility
- [ ] Add content descriptions to all buttons
- [ ] Ensure 48dp minimum touch targets
- [ ] Verify color contrast ratios (WCAG AA)
- [ ] Test with TalkBack screen reader
- [ ] Add haptic feedback where appropriate

**Expected Outcome:** App is accessible

### Task 9.2: Error Handling
- [ ] Add error dialogs for all failure cases
- [ ] Show helpful error messages
- [ ] Handle low storage gracefully
- [ ] Handle corrupted files
- [ ] Test all error paths

**Expected Outcome:** Errors handled gracefully

### Task 9.3: ACRA Setup
- [ ] Add ACRA dependencies
- [ ] Configure ACRA in Application class
- [ ] Create crash report dialog
- [ ] Test crash reporting
- [ ] Verify no automatic upload
- [ ] Test manual report sharing

**Expected Outcome:** Crash reports saved locally

### Task 9.4: Documentation
- [ ] Write README.md with app description
- [ ] Add build instructions
- [ ] Document file formats
- [ ] Create user guide (basic usage)
- [ ] Add LICENSE file
- [ ] Add PRIVACY.md (no data collection)

**Expected Outcome:** Project is documented

### Task 9.5: CI/CD Pipeline
- [ ] Create `.github/workflows/android-ci.yml`
- [ ] Configure Gradle wrapper validation
- [ ] Add build job (assembleDebug)
- [ ] Add lint job (lintDebug)
- [ ] Add test job (testDebugUnitTest)
- [ ] Add coverage reporting (Jacoco)
- [ ] Upload APK artifact
- [ ] Test CI pipeline

**Expected Outcome:** CI pipeline runs on push/PR

## Phase 10: Release Preparation

### Task 10.1: Final Testing
- [ ] Full regression test on all features
- [ ] Test all user workflows end-to-end
- [ ] Verify all acceptance criteria met
- [ ] Fix any remaining bugs

**Expected Outcome:** App is stable and complete

### Task 10.2: Release Build
- [ ] Configure release build type
- [ ] Set up signing configuration
- [ ] Build release APK
- [ ] Test release build
- [ ] Verify APK size is reasonable

**Expected Outcome:** Release APK ready

### Task 10.3: F-Droid Preparation
- [ ] Verify no proprietary dependencies
- [ ] Ensure reproducible builds
- [ ] Create F-Droid metadata
- [ ] Test F-Droid build process

**Expected Outcome:** App ready for F-Droid

## Summary

**Total Tasks:** ~100
**Estimated Duration:** 8-10 weeks
**Team Size:** 1 developer

**Key Milestones:**
- Week 2: Sphere renders and rotates
- Week 4: All tools working with undo/redo
- Week 6: Save/load and STL export complete
- Week 8: Polished and ready for release

**Risk Mitigation:**
- Week 1-2 is proof of concept (validate OpenGL performance)
- Can adjust scope if timeline slips
- Core features prioritized over polish
