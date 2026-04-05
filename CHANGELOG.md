# Changelog

All notable changes to ClayModeller will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2025-07-18

### Added
- 9 sculpting tools: Remove, Add, Pull, Smooth, Flatten, Pinch, Inflate, Light, View
- Drag-based sculpting for Add and Pull tools
- Per-model lighting control (position and intensity)
- Interactive light mode
- 6 built-in example models
- Undo/Redo (up to 20 levels)
- Save/Load models in .clay format
- Save to external storage
- STL export for 3D printing (50-200mm)
- Export wizard with attachment placement
- Preset manager for export configurations
- Auto-save every minute
- Visual tool cursor
- Symmetry line for mirrored sculpting
- Tooltips on tool buttons
- Camera controls (rotate, zoom, pan, double-tap reset)
- Brush size and strength settings
- Accessibility support
- Crash reporting (ACRA)
- GitHub Actions CI (build, lint, test, release APK)

### Technical
- OpenGL ES 3.0 rendering
- Phong lighting
- Ray casting for tool interaction
- Octree spatial acceleration
- Binary .clay format with CRC32 checksums
- Atomic file writes
- MediaStore API for Android 10+
- Geometry generation and caching for export attachments
- Surface picker for interactive placement
