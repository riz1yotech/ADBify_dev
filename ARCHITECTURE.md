# Adbify Architecture Documentation

This document provides a detailed technical overview of the Adbify codebase architecture and workflow for developers who want to understand or contribute to the project.

## Table of Contents

- [Project Structure](#project-structure)
- [Architecture Overview](#architecture-overview)
- [Module Breakdown](#module-breakdown)
- [Data Flow](#data-flow)
- [Component Details](#component-details)
- [Native Layer](#native-layer)
- [Terminal Emulation](#terminal-emulation)
- [Lifecycle Management](#lifecycle-management)
- [File Operations](#file-operations)
- [Permission Handling](#permission-handling)
- [Build Configuration](#build-configuration)

## Project Structure

```
Adbify/
├── app/                          # Main application module
│   ├── src/main/
│   │   ├── java/com/adbify/
│   │   │   ├── AdbifyApp.kt      # Application class
│   │   │   ├── MainActivity.kt   # Main UI activity
│   │   │   ├── app/              # Base activity classes
│   │   │   ├── terminal/         # Terminal integration
│   │   │   └── utils/            # Utility classes
│   │   ├── jniLibs/              # Native ADB binaries
│   │   │   ├── arm64-v8a/        # ARM 64-bit
│   │   │   ├── armeabi-v7a/      # ARM 32-bit
│   │   │   ├── x86/              # x86 32-bit
│   │   │   └── x86_64/           # x86 64-bit
│   │   └── res/                  # Resources
│   └── build.gradle              # App build config
├── terminalview/                 # Terminal emulator module
│   ├── src/main/
│   │   ├── java/com/adbify/terminal/
│   │   │   ├── TerminalSession.java    # PTY session
│   │   │   ├── TerminalEmulator.java   # Terminal emulation
│   │   │   ├── TerminalBuffer.java     # Screen buffer
│   │   │   └── view/                   # UI components
│   │   └── jni/                  # Native code
│   └── build.gradle              # Module build config
└── build.gradle                  # Root build config
```

## Architecture Overview

Adbify follows a layered architecture with clear separation of concerns:

```
┌──────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│  ┌────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │MainActivity│  │AppBarActivity│  │    AdbifyApp     │  │
│  └────────────┘  └──────────────┘  └──────────────────┘  │
└──────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────────────────────────────────────┐
│                       Service Layer                      │
│  ┌───────────────────────────────────────────────────┐   │
│  │              TerminalService                      │   │
│  │  (Foreground Service - Manages Sessions)          │   │
│  └───────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────────────────────────────────────┐
│                   Terminal Emulation Layer               │
│  ┌───────────────┐  ┌────────────────┐  ┌─────────────┐  │
│  │TerminalSession│  │TerminalEmulator│  │TerminalView │  │
│  └───────────────┘  └────────────────┘  └─────────────┘  │
└──────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────────────────────────────────────┐
│                       Native Layer (JNI)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │ PTY Creation │  │ Process Exec │  │ I/O Handling │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└──────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────────────────────────────────────┐
│                      Operating System                    │
│  ┌───────────────────────────────────────────────────┐   │
│  │   Shell Process (/system/bin/sh) + ADB Binary     │   │
│  └───────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

## Module Breakdown

### 1. App Module (`app/`)

Main application module containing UI, business logic, and service components.

#### Key Components:

**AdbifyApp.kt**
- Application entry point
- Lifecycle observer pattern implementation
- Ad management integration (Google Mobile Ads)
- Activity lifecycle callbacks
- Global app state management

```kotlin
class AdbifyApp : Application(), ActivityLifecycleCallbacks, DefaultLifecycleObserver
```

**MainActivity.kt**
- Main UI controller
- Binds to TerminalService
- Handles user interactions (menu, file attachment, sharing)
- Terminal view management
- Permission requests
- File URI handling and caching

**TerminalService.kt**
- Foreground service for persistent sessions
- Creates notification channel
- Manages terminal session lifecycle
- Sets up ADB environment:
  - Creates symlink to native ADB binary
  - Sets environment variables (TERM, HOME, PATH, ADB)
  - Initializes PTY session

**Utility Classes:**
- `PermissionHelper.kt`: Storage permission handling for Android 11+ (MANAGE_EXTERNAL_STORAGE)
- `FileUtils.java`: File operations, URI to path conversion
- `AndroidUtilities.java`: Toast helpers, general utilities
- `AppIconCache.kt`: App icon loading and caching

### 2. Terminal View Module (`terminalview/`)

Custom terminal emulator implementation based on VT100/ANSI standards.

#### Core Components:

**TerminalSession.java**
- Manages PTY (pseudo-terminal) session
- Handles subprocess creation
- I/O queue management (bidirectional)
- Process lifecycle management
- Uses JNI to create subprocess with PTY

```java
// Key responsibilities:
- Create PTY using JNI.createSubprocess()
- Manage mProcessToTerminalIOQueue (output from shell)
- Manage mTerminalToProcessIOQueue (input to shell)
- Handle process exit events
```

**TerminalEmulator.java**
- Terminal emulation engine
- Processes ANSI/VT100 escape sequences
- Maintains terminal state (cursor position, colors, etc.)
- Handles control characters
- Screen buffer management

**TerminalBuffer.java**
- Screen buffer implementation
- Stores terminal rows (TerminalRow objects)
- Handles scrolling and history
- Manages screen dimensions

**TerminalView.java**
- Custom Android View for terminal display
- Renders terminal buffer to canvas
- Handles touch input, gestures, scrolling
- Text selection support
- Keyboard input handling

**TerminalRenderer.java**
- Rendering engine for terminal text
- Handles font rendering, colors, and styling
- Supports text attributes (bold, italic, underline)

## Data Flow

### Complete User Input to Output Flow

```
1. USER INPUT
   User types "adb devices" → TerminalView.onKey()
                                    ↓
2. INPUT PROCESSING
   TerminalView converts to bytes → UTF-8 encoding
                                    ↓
3. QUEUE TO SHELL
   Bytes written to → mTerminalToProcessIOQueue
                                    ↓
4. WRITE THREAD
   Background thread reads queue → Writes to PTY file descriptor
                                    ↓
5. SHELL EXECUTION
   Shell process receives input → Executes ADB command
                                    ↓
6. OUTPUT GENERATION
   ADB produces output → Writes to PTY (stdout/stderr)
                                    ↓
7. READ THREAD
   Background thread reads PTY → Writes to mProcessToTerminalIOQueue
                                    ↓
8. EMULATION
   Main thread processes queue → TerminalEmulator.append()
                                    ↓
9. BUFFER UPDATE
   Emulator updates → TerminalBuffer (screen state)
                                    ↓
10. VIEW RENDER
    TerminalView.invalidate() → TerminalRenderer draws to Canvas
                                    ↓
11. DISPLAY
    User sees output → "List of devices attached..."
```

### Service Binding Flow

```
MainActivity.onCreate()
    ↓
startService(TerminalService)
    ↓
bindService(TerminalService)
    ↓
onServiceConnected(IBinder binder)
    ↓
terminalService = binder.getService()
    ↓
terminalService.setTerminalSessionClient(client)
    ↓
session = terminalService.getOrCreateTerminalSession()
    ↓
terminalView.attachSession(session)
```

### File Attachment Flow

```
User clicks "Attachment" menu
    ↓
MainActivity.attachNewFile()
    ↓
Check storage permissions
    ↓
Launch file picker (GetContentContract)
    ↓
User selects file → Returns URI
    ↓
MainActivity.handleFileUri(uri)
    ↓
Try direct path access → FileUtils.getPath()
    ↓
If fails → Copy to cache directory
    ↓
FileUtils.copyUriToPath(uri, cacheDir)
    ↓
Paste file path to terminal → session.emulator.paste(path)
    ↓
User can use path in ADB commands
```

## Component Details

### TerminalService Initialization

**ADB Setup Process:**

```kotlin
// 1. Create home directory
private val adbHomeDir: String
    get() {
        val homeDir = File(filesDir, "home")
        if (!homeDir.exists()) homeDir.mkdirs()
        return homeDir.absolutePath
    }

// 2. Create symlink to ADB binary
private val ADB: String
    get() {
        val binDir = File(filesDir, "bin")
        binDir.deleteRecursively()
        if (!binDir.exists()) binDir.mkdirs()
        
        // Symlink libadb.so to make it executable as "adb"
        Os.symlink(
            "${applicationInfo.nativeLibraryDir}/libadb.so",
            "${binDir.absolutePath}/adb"
        )
        return binDir.resolve("adb").absolutePath
    }

// 3. Set up environment variables
val envs = arrayOfNulls<String>(5)
envs[0] = "TERM=screen"              // Terminal type
envs[1] = "HOME=$adbHomeDir"         // Home directory
envs[2] = "TMPDIR=${cacheDir.absolutePath}"  // Temp directory
envs[3] = "ADB=$ADB"                 // ADB binary path
envs[4] = "PATH=${Os.getenv("PATH")}:${ADB.substring(0, ADB.lastIndexOf("/"))}"

// 4. Create terminal session
terminalSession = TerminalSession(
    "/system/bin/sh",    // Shell executable
    "/",                 // Working directory
    arrayOf<String>(),   // Shell arguments
    envs,                // Environment variables
    TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
    terminalSessionActivityClient
)
```

**Why Symlink?**
- Android doesn't allow executing files from app directories
- Native libraries (.so files) can be loaded by system
- Creating symlink makes the native library executable as a regular binary

### Terminal Session Threading Model

**Three Main Threads:**

1. **Main Thread (UI Thread)**
   - Handles all UI updates
   - Processes terminal emulation
   - Updates TerminalView
   - Handles user input events

2. **Input Thread (Terminal to Process)**
   - Reads from `mTerminalToProcessIOQueue`
   - Writes to PTY file descriptor
   - Blocks on write operations

3. **Output Thread (Process to Terminal)**
   - Reads from PTY file descriptor
   - Writes to `mProcessToTerminalIOQueue`
   - Sends MSG_NEW_INPUT to main thread handler
   - Blocks on read operations

**Thread Synchronization:**
- ByteQueue provides thread-safe queue operations
- Handler messages coordinate cross-thread communication
- FileDescriptor operations are inherently thread-safe

### Terminal Emulator State Machine

The emulator processes escape sequences as a state machine:

```
[Normal Text Mode] → Character received
    │
    ├─ Regular char → Write to buffer
    ├─ '\n' (newline) → Move cursor down, reset column
    ├─ '\r' (carriage return) → Reset cursor to column 0
    ├─ ESC → Enter [Escape Mode]
    │
[Escape Mode] → Next character determines sequence type
    │
    ├─ '[' → Enter [CSI Mode] (Control Sequence Introducer)
    ├─ ']' → Enter [OSC Mode] (Operating System Command)
    ├─ Other → Execute escape command
    │
[CSI Mode] → Parse parameters and execute
    │
    ├─ 'A' → Cursor up
    ├─ 'B' → Cursor down
    ├─ 'C' → Cursor right
    ├─ 'D' → Cursor left
    ├─ 'H' → Cursor position
    ├─ 'm' → Set graphics mode (colors, bold, etc.)
    ├─ 'J' → Erase display
    └─ 'K' → Erase line
```

## Native Layer

### JNI Interface

**JNI.java** defines native methods:

```java
public class JNI {
    static {
        System.loadLibrary("termux");  // Load native library
    }
    
    /**
     * Create a subprocess with PTY
     * @return Array: [pid, master_fd]
     */
    public static native int[] createSubprocess(
        String cmd,           // Command to execute
        String cwd,           // Working directory
        String[] args,        // Command arguments
        String[] env,         // Environment variables
        int[] processId,      // Output: process ID
        int rows,            // Terminal rows
        int cols             // Terminal columns
    );
    
    public static native void setPtyWindowSize(int fd, int rows, int cols);
    public static native void setPtyUTF8Mode(int fd, boolean utf8);
    public static native int waitFor(int pid);
    public static native void close(int fd);
}
```

**Native Implementation (jni/):**
- Creates PTY master/slave pair
- Forks process
- Sets up file descriptors (stdin, stdout, stderr)
- Executes command in child process
- Returns master FD for I/O operations

### ADB Binary Integration

**Pre-compiled Binaries:**
- Located in `app/src/main/jniLibs/[arch]/libadb.so`
- Compiled from AOSP (Android Open Source Project) ADB source
- Includes all ADB functionality (server, client, protocols)

**Runtime Integration:**
1. App installation: System extracts native libraries
2. Libraries placed in: `/data/app/.../lib/[arch]/`
3. Service creates symlink: `/data/data/com.adbify/files/bin/adb`
4. Shell PATH includes bin directory
5. User can execute: `adb devices`, `adb shell`, etc.

## Lifecycle Management

### Activity Lifecycle

```
MainActivity Lifecycle:
    onCreate()
        ↓
    Setup UI, bind service
        ↓
    onStart() → Set isVisible = true
        ↓
    onResume() → Resume terminal session
        ↓
    [User interacts]
        ↓
    onStop() → Set isVisible = false
        ↓
    onDestroy() → Unbind service, cleanup
```

### Service Lifecycle

```
TerminalService Lifecycle:
    onCreate()
        ↓
    Start foreground with notification
        ↓
    onBind() → Return LocalBinder
        ↓
    Activity binds → Create/get terminal session
        ↓
    [Service runs in foreground]
        ↓
    onUnbind() → Unset session client
        ↓
    stopService() or ACTION_STOP_SERVICE
        ↓
    Cleanup session, stop foreground
        ↓
    onDestroy()
```

### Session Lifecycle

```
TerminalSession Lifecycle:
    Constructor → Store parameters
        ↓
    First updateSize() call
        ↓
    Create subprocess via JNI
        ↓
    Start I/O threads
        ↓
    [Session active]
        ↓
    finishIfRunning() or process exits
        ↓
    Kill process, close FD
        ↓
    Notify client via onSessionFinished()
```

## File Operations

### Permission Handling Flow

**Android 11+ (API 30+):**
```kotlin
// Request MANAGE_EXTERNAL_STORAGE
Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
    .setData("package:${packageName}".toUri())
```

**Android 10 and below:**
```kotlin
// Request READ/WRITE_EXTERNAL_STORAGE
requestPermissions(
    arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
)
```

### File URI to Path Conversion

**FileUtils.getPath()**:
1. Check if URI is file:// scheme → Extract path directly
2. Check if URI is content:// scheme:
   - Query ContentResolver for file path
   - Handle different content providers (Downloads, MediaStore, etc.)
3. If direct access fails → Copy file to cache directory

### Cache Directory Usage

When direct file access is not possible:
```
Source URI → InputStream
    ↓
Read chunks → Write to cache file
    ↓
Cache: /data/data/com.adbify/cache/adb_files_cache/[filename]
    ↓
Return cache file path to terminal
```

## Permission Handling

### PermissionHelper Implementation

**Modern Permission Approach:**

```kotlin
class PermissionHelper(private val activity: AppCompatActivity) {
    
    // Launcher for Android 11+
    private var manageStoragePermissionLauncher: ActivityResultLauncher<Intent>?
    
    // Launcher for Android 10-
    private var storagePermissionLauncher: ActivityResultLauncher<Array<String>>?
    
    fun registerPermissionLaunchers() {
        // Must be called before onCreate() completes
        // ActivityResultContracts handle permission callbacks
    }
    
    fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Request MANAGE_EXTERNAL_STORAGE
        } else {
            // Request READ/WRITE_EXTERNAL_STORAGE
        }
    }
}
```

**Why ActivityResultLauncher?**
- Modern replacement for deprecated `onRequestPermissionsResult()`
- Better lifecycle handling
- Type-safe contracts
- Cleaner callback mechanism

## Build Configuration

### Gradle Structure

**Root build.gradle:**
- Defines plugin versions
- Global build configurations
- Shared version properties (compileSdk, targetSdk, versionCode)

**App build.gradle:**
- Application configuration
- Dependencies
- Material Theme Builder plugin
- ProGuard rules for release builds
- ABI splits for architecture-specific APKs

**Terminal View build.gradle:**
- Library module configuration
- NDK build settings
- Native code compilation (Android.mk)

### Multi-Architecture Build

**ABI Splits:**
```groovy
splits {
    abi {
        enable true
        reset()
        include 'x86', 'x86_64', 'armeabi-v7a', 'arm64-v8a'
        universalApk true  // Also create universal APK
    }
}
```

**Outputs:**
- `app-arm64-v8a-release.apk` (ARM 64-bit)
- `app-armeabi-v7a-release.apk` (ARM 32-bit)
- `app-x86-release.apk` (Intel 32-bit)
- `app-x86_64-release.apk` (Intel 64-bit)
- `app-universal-release.apk` (All architectures)

### ProGuard Configuration

**Release Build Optimizations:**
- Code shrinking: Remove unused classes/methods
- Resource shrinking: Remove unused resources
- Obfuscation: Rename classes/methods
- Optimization: Optimize bytecode

Keep rules preserve:
- Terminal emulator classes (reflection usage)
- Native method declarations
- Service entry points
- Serializable classes

## Key Design Patterns

### 1. Service-Bound Architecture
- Activity binds to Service for long-running operations
- Service manages session lifecycle independently
- Loose coupling via IBinder interface

### 2. Observer Pattern
- TerminalSessionClient interface for callbacks
- View updates triggered by session events
- Lifecycle observers for app state

### 3. Thread-Safe Queues
- ByteQueue for cross-thread communication
- Lock-free reads/writes where possible
- Handler for thread synchronization

### 4. View-Model Separation
- TerminalView (UI) separate from TerminalEmulator (logic)
- TerminalBuffer as data model
- Clear separation of concerns

### 5. Dependency Injection
- Service binder provides access to session
- Clients injected into session for callbacks
- Facilitates testing and modularity

## Performance Considerations

### Optimizations

1. **Efficient Rendering:**
   - Only redraw changed portions of terminal
   - Use hardware acceleration where possible
   - Batch rendering operations

2. **Memory Management:**
   - Circular buffer for terminal history
   - Limit transcript rows (configurable)
   - Recycle objects where possible

3. **I/O Efficiency:**
   - Buffered reads/writes
   - Native code for performance-critical operations
   - Async file operations with coroutines

4. **Thread Management:**
   - Reuse threads for session I/O
   - Main thread only for UI updates
   - Minimize cross-thread communication overhead

## Security Considerations

1. **File Access:**
   - Scoped storage compliance
   - Validate file paths
   - Use FileProvider for sharing

2. **Process Isolation:**
   - App runs with own UID
   - Cannot access other app data without permissions
   - SELinux enforces restrictions

3. **ADB Security:**
   - ADB commands run with app's permissions
   - Cannot perform privileged operations without root
   - Network operations require appropriate permissions

## Testing Strategy

### Unit Tests
- Terminal emulator logic
- Escape sequence parsing
- Buffer operations
- Utility functions

### Integration Tests
- Service binding
- Session lifecycle
- File operations
- Permission handling

### UI Tests
- Terminal view interaction
- Menu operations
- File selection
- Text selection

## Future Enhancement Possibilities

1. **Custom Key Mappings:** User-configurable keyboard shortcuts
2. **Themes:** Custom color schemes for terminal
3. **Plugins:** Extension system for additional functionality
4. **Cloud Sync:** Sync terminal history/preferences
5. **Multi-Session:** Multiple terminal tabs
6. **SSH Support:** Connect to remote devices
7. **Script Automation:** Record and replay command sequences

## Contributing Guidelines

When contributing to Adbify:

1. **Follow Android Best Practices:**
   - Use Kotlin for new code
   - Follow Material Design guidelines
   - Handle configuration changes properly

2. **Code Style:**
   - Follow existing formatting
   - Add KDoc/JavaDoc for public APIs
   - Use meaningful variable names

3. **Testing:**
   - Add tests for new features
   - Ensure existing tests pass
   - Test on multiple Android versions

4. **Performance:**
   - Profile performance-critical changes
   - Avoid memory leaks
   - Optimize for battery usage

5. **Documentation:**
   - Update README for user-facing changes
   - Update ARCHITECTURE for technical changes
   - Comment complex logic

## Resources

- [Android Terminal Emulator](https://github.com/jackpal/Android-Terminal-Emulator)
- [Termux](https://github.com/termux/termux-app)
- [VT100 Terminal Documentation](https://vt100.net/)
- [ANSI Escape Codes](https://en.wikipedia.org/wiki/ANSI_escape_code)
- [Android ADB Source](https://android.googlesource.com/platform/packages/modules/adb/)

---

**Last Updated:** December 2025  
**Adbify Version:** 2.6.0 (Build 40)

