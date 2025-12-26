# Adbify

<p align="center">
  <img src="logo.png" alt="Adbify Logo" width="200"/>
</p>

**Adbify** is an Android application that provides a full-featured ADB (Android Debug Bridge) terminal emulator running directly on your Android device. Execute ADB commands without needing a computer, making it perfect for developers, power users, and testers who need ADB access on-the-go.

## 📋 Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [How to Connect Devices (ADB Setup)](#how-to-connect-devices-adb-setup)
  - [Wired Connection](#wired-connection)
  - [Wireless Connection](#wireless-connection)
- [How to Run the Project](#how-to-run-the-project)
- [How It Works](#how-it-works)
- [Limitations](#limitations)
- [Common Issues and Solutions](#common-issues-and-solutions)
- [FAQ](#faq)
- [License](#license)

## ✨ Features

- **Native ADB Shell**: Run ADB commands directly on your Android device
- **Full Terminal Emulator**: Complete terminal interface with text selection, scrolling, and keyboard support
- **File Attachment**: Easily attach files from device storage to use in ADB commands
- **Persistent Session**: Foreground service keeps your terminal session alive
- **Material Design 3**: Modern, adaptive UI following Material You guidelines
- **Keep Screen On**: Option to prevent screen timeout during long operations
- **Share Transcripts**: Export terminal output to share or save
- **Multi-Architecture Support**: Works on ARM, ARM64, x86, and x86_64 devices

## 📱 Requirements

- **Minimum Android Version**: Android 7.0 (API 24)
- **Target Android Version**: Android 13 (API 33)
- **Storage Permission**: Required for file operations
- **Root Access**: Not required for basic ADB operations on the device itself
- **Architecture**: ARM, ARM64, x86, or x86_64

## 🚀 Installation

1. Download the latest APK from the releases page
2. Enable "Install from Unknown Sources" in your device settings
3. Install the APK
4. Grant storage permissions when prompted
5. Launch Adbify

## 🔌 How to Connect Devices (ADB Setup)

Adbify runs ADB commands **on the device itself**. To connect to other devices:

### Wired Connection

#### Using USB OTG (Connect Android to Android)

1. **On Target Device** (device to be controlled):
   ```bash
   # Enable Developer Options
   Settings → About Phone → Tap "Build Number" 7 times
   
   # Enable USB Debugging
   Settings → Developer Options → USB Debugging → ON
   ```

2. **On Adbify Device** (device running Adbify):
   - Connect target device using USB OTG cable
   - Target device will show "Allow USB debugging?" prompt
   - Check "Always allow" and tap "OK"

3. **In Adbify Terminal**:
   ```bash
   # Check if device is connected
   adb devices
   
   # You should see output like:
   # List of devices attached
   # XXXXXXXXXX    device
   ```

4. **Start Using ADB**:
   ```bash
   # Install an APK
   adb install /sdcard/app.apk
   
   # Access shell on connected device
   adb shell
   
   # Take screenshot
   adb shell screencap -p /sdcard/screenshot.png
   adb pull /sdcard/screenshot.png
   ```

### Wireless Connection

#### Method 1: Connect via WiFi (Both Devices on Same Network)

1. **On Target Device**:
   ```bash
   # Enable Developer Options and USB Debugging first
   # Then connect to computer or another device with ADB
   adb tcpip 5555
   ```

2. **Get Target Device IP**:
   ```bash
   Settings → About Phone → Status → IP Address
   # Or use: adb shell ip addr show wlan0
   ```

3. **In Adbify Terminal**:
   ```bash
   # Connect to target device (replace with actual IP)
   adb connect 192.168.1.100:5555
   
   # Verify connection
   adb devices
   ```

4. **Disconnect**:
   ```bash
   adb disconnect 192.168.1.100:5555
   ```

#### Method 2: Wireless Debugging (Android 11+)

1. **On Target Device**:
   ```bash
   Settings → Developer Options → Wireless Debugging → ON
   ```

2. **Get Pairing Code**:
   ```bash
   Wireless Debugging → Pair device with pairing code
   # Note the IP address and port (e.g., 192.168.1.100:12345)
   ```

3. **In Adbify Terminal**:
   ```bash
   # Pair with code
   adb pair 192.168.1.100:12345
   # Enter the 6-digit pairing code when prompted
   
   # Connect (use the port shown under "IP address & Port")
   adb connect 192.168.1.100:5555
   ```

### Common ADB Commands

```bash
# Device Information
adb devices                           # List connected devices
adb shell getprop ro.build.version.release  # Android version
adb shell getprop ro.product.model    # Device model

# File Operations
adb push /sdcard/file.txt /data/local/tmp/  # Copy to device
adb pull /sdcard/file.txt             # Copy from device
adb shell ls /sdcard/                 # List files

# App Management
adb install app.apk                   # Install app
adb install -r app.apk                # Reinstall app
adb uninstall com.example.app         # Uninstall app
adb shell pm list packages            # List all packages

# System Operations
adb shell dumpsys battery             # Battery info
adb shell wm size                     # Screen resolution
adb shell screencap -p /sdcard/screen.png  # Screenshot
adb shell screenrecord /sdcard/video.mp4   # Screen record

# Logcat
adb logcat                            # View logs
adb logcat -c                         # Clear logs
adb logcat *:E                        # Show errors only
```

## 🛠️ How to Run the Project

### Prerequisites

- **Android Studio**: Arctic Fox or newer
- **JDK**: Java 8 or higher
- **Android SDK**: API 33 (Android 13)
- **NDK**: Version 25.2.9519653
- **Gradle**: 8.11.2 or newer

### Build Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Riz1yotech/ADBify_dev.git
   cd ADBify_dev
   ```

2. **Open in Android Studio**:
   - Launch Android Studio
   - File → Open → Select the Adbify directory

3. **Configure SDK**:
   - Ensure Android SDK API 33 is installed
   - Install NDK version 25.2.9519653
   - Tools → SDK Manager → SDK Tools → NDK

4. **Sync Gradle**:
   - Android Studio will prompt to sync
   - Or manually: File → Sync Project with Gradle Files

5. **Build the Project**:
   
   **Option A - Using Android Studio**:
   - Build → Make Project (Ctrl+F9)
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   
   **Option B - Using Command Line**:
   ```bash
   # Debug build
   ./gradlew assembleDebug
   
   # Release build (signed with project keystore)
   ./gradlew assembleRelease
   
   # Install to connected device
   ./gradlew installDebug
   ```

6. **Run on Device/Emulator**:
   - Connect Android device or start emulator
   - Run → Run 'app' (Shift+F10)
   - Or: `./gradlew installDebug`

### Building Signed Release APK

The project includes a pre-configured keystore for public releases. The release APK will be automatically signed when you build.

#### Quick Build (Recommended)

```bash
# Build signed release APK
./gradlew assembleRelease

# APK will be generated at:
# app/build/outputs/apk/release/app-release.apk
```

#### Keystore Configuration

The project uses `key.properties` file for signing configuration:

```properties
storeFile=/keystore.jks
jksPassword=123456
keyAlias=123456
keyPassword=123456
```

**Note**: This is a public keystore for development and testing purposes. For production apps on Google Play Store, you should create your own keystore and keep it secure.

#### Creating Your Own Keystore (Optional)

If you want to use your own keystore for production:

1. **Generate a new keystore**:
   ```bash
   keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
   ```

2. **Update `key.properties`**:
   ```properties
   storeFile=my-release-key.jks
   jksPassword=your_store_password
   keyAlias=my-key-alias
   keyPassword=your_key_password
   ```

3. **Rebuild**:
   ```bash
   ./gradlew clean assembleRelease
   ```

#### Build All Architecture Variants

The project is configured to build separate APKs for each architecture plus a universal APK:

```bash
# Build all variants
./gradlew assembleRelease

# Output APKs:
# app/build/outputs/apk/release/
#   ├── app-arm64-v8a-release.apk      (ARM 64-bit - ~8MB)
#   ├── app-armeabi-v7a-release.apk    (ARM 32-bit - ~7MB)
#   ├── app-x86-release.apk            (Intel 32-bit - ~8MB)
#   ├── app-x86_64-release.apk         (Intel 64-bit - ~9MB)
#   └── app-universal-release.apk      (All architectures - ~30MB)
```

**Recommended for distribution**:
- **Universal APK** (`app-universal-release.apk`) - Works on all devices
- **Architecture-specific APKs** - Smaller size, upload all to GitHub releases

#### Verify APK Signature

```bash
# Check if APK is properly signed
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Should show: "jar verified."
```

#### Build and Install Release APK

```bash
# Build and install release APK to connected device
./gradlew installRelease

# Or manually install
adb install -r app/build/outputs/apk/release/app-universal-release.apk
```

### Build Outputs

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK (Universal)**: `app/build/outputs/apk/release/app-universal-release.apk`
- **Release APK (ARM64)**: `app/build/outputs/apk/release/app-arm64-v8a-release.apk`
- **Release APK (ARM32)**: `app/build/outputs/apk/release/app-armeabi-v7a-release.apk`
- **Release APK (x86)**: `app/build/outputs/apk/release/app-x86-release.apk`
- **Release APK (x86_64)**: `app/build/outputs/apk/release/app-x86_64-release.apk`

### Release Checklist

Before publishing a release, ensure:

- [ ] **Version updated** in `build.gradle`:
  ```groovy
  versionCode = 41  // Increment for each release
  versionName = "2.7.0"  // Follow semantic versioning
  ```
- [ ] **Build release APK**: `./gradlew assembleRelease`
- [ ] **Test on device**: Install and verify functionality
- [ ] **Check APK size**: Ensure it's optimized (~8-10MB per architecture)
- [ ] **Verify signature**: APK is properly signed
- [ ] **Update changelog**: Document what's new
- [ ] **Commit changes**: Push to GitHub
- [ ] **Create release**: Tag and upload APK to GitHub Releases

### Troubleshooting Build Issues

**Issue: "Keystore not found"**
```bash
# Ensure key.properties and keystore.jks are in the project root
ls -la keystore.jks key.properties
```

**Issue: "Signing config missing"**
```bash
# Sync Gradle files
./gradlew --refresh-dependencies
```

**Issue: "Build failed with ProGuard errors"**
```bash
# Clean and rebuild
./gradlew clean assembleRelease --stacktrace
```

**Issue: "NDK not found"**
- Install NDK version 25.2.9519653 via SDK Manager
- Or set `ANDROID_NDK_HOME` environment variable

## 🔍 How It Works

For a detailed technical explanation of the codebase architecture and workflow, see [ARCHITECTURE.md](ARCHITECTURE.md).

### High-Level Overview

1. **Application Layer** (`AdbifyApp.kt`):
   - Manages app lifecycle
   - Handles ad integration
   - Coordinates activity lifecycle callbacks

2. **UI Layer** (`MainActivity.kt`):
   - Main terminal interface
   - Handles user interactions
   - File attachment and sharing
   - Manages terminal view display

3. **Terminal Service** (`TerminalService.kt`):
   - Foreground service for persistent sessions
   - Creates and manages terminal sessions
   - Sets up ADB environment
   - Manages process lifecycle

4. **Terminal Emulator** (`terminalview` module):
   - Native terminal emulation
   - PTY (pseudo-terminal) handling
   - VT100/ANSI escape sequence processing
   - Text rendering and selection

5. **Native Layer** (JNI):
   - ADB binary (`libadb.so`) compiled for each architecture
   - PTY creation and management
   - Process execution and I/O handling

### Data Flow

```
User Input → TerminalView → TerminalSession → PTY → Shell Process (ADB)
                                                              ↓
User Display ← TerminalView ← TerminalEmulator ← PTY ← Output
```

## ⚠️ Limitations

### Technical Limitations

1. **No Root Operations**: 
   - Cannot execute commands requiring root privileges unless device is rooted
   - Cannot access system partitions without root

2. **USB OTG Required for Device-to-Device**:
   - Physical connection needs USB OTG cable
   - Not all devices support USB OTG

3. **WiFi Same Network Requirement**:
   - Wireless ADB requires both devices on same network
   - Cannot connect across different networks without advanced configuration

4. **ADB Server Limitations**:
   - Running as regular app, not system service
   - May have limited access to certain device features

5. **Storage Access**:
   - Scoped storage restrictions on Android 11+
   - Requires MANAGE_EXTERNAL_STORAGE for full access

### Known Issues

1. **Battery Optimization**:
   - Background service may be killed by aggressive battery savers
   - Add app to battery optimization whitelist

2. **Keyboard Issues**:
   - Some custom keyboards may not work properly
   - Use system keyboard for best results

3. **File Picker Limitations**:
   - Cannot access all file system locations on newer Android versions
   - Use cache directory for temporary files

4. **Screen Rotation**:
   - Terminal session state maintained across rotations
   - View may need to reattach to session

## 🔧 Common Issues and Solutions

### Issue 1: "ADB devices" shows empty list

**Symptoms**: No devices appear when running `adb devices`

**Solutions**:
```bash
# 1. Restart ADB server
adb kill-server
adb start-server

# 2. Check USB debugging on target device
# Ensure USB debugging is enabled in Developer Options

# 3. Verify USB connection
# Check if USB cable supports data transfer (not charge-only)

# 4. Check USB mode
# On target device, change USB mode to "File Transfer" or "PTP"
```

### Issue 2: "Permission Denied" errors

**Symptoms**: Cannot execute commands or access files

**Solutions**:
```bash
# 1. Check app permissions
# Settings → Apps → Adbify → Permissions → Enable Storage

# 2. For file operations, use accessible directories
# Use /sdcard/ or app's private directory
cd /sdcard/
cd /data/local/tmp/

# 3. Some operations require device to be rooted
# Root access needed for system file modifications
```

### Issue 3: Terminal session keeps stopping

**Symptoms**: App loses terminal session in background

**Solutions**:
1. **Disable Battery Optimization**:
   ```
   Settings → Battery → Battery Optimization
   → Select "All apps" → Find Adbify → Don't optimize
   ```

2. **Enable Keep Screen On**:
   - In Adbify: Menu (⋮) → Keep Screen On

3. **Lock App in Recent Apps**:
   - Recent apps → Adbify → Lock icon

### Issue 4: Cannot connect wirelessly

**Symptoms**: `adb connect` fails or times out

**Solutions**:
```bash
# 1. Verify both devices on same WiFi network
# Check WiFi settings on both devices

# 2. Check firewall/port blocking
# Ensure port 5555 is not blocked

# 3. Verify IP address is correct
# On target device:
adb shell ip addr show wlan0

# 4. Restart wireless debugging
# On target device:
adb tcpip 5555
# Then connect from Adbify device
```

### Issue 5: "Cannot attach file" error

**Symptoms**: File attachment fails

**Solutions**:
1. Grant storage permissions when prompted
2. For Android 11+, enable "All files access":
   ```
   Settings → Apps → Adbify → Permissions
   → Files and media → Allow management of all files
   ```
3. File will be copied to `/data/data/com.adbify/cache/adb_files_cache/`
4. Use the path shown in terminal

### Issue 6: Build errors after Material library update

**Symptoms**: Resource linking errors, missing attributes

**Solutions**:
1. **Clean and rebuild**:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

2. **Update Material Components**:
   - Ensure `material` library is version 1.11.0 or higher in `app/build.gradle`

3. **Sync Gradle files**:
   - File → Sync Project with Gradle Files

### Issue 7: App crashes on launch

**Symptoms**: App force closes immediately

**Solutions**:
1. Check Android version (must be 7.0+)
2. Clear app data:
   ```
   Settings → Apps → Adbify → Storage → Clear Data
   ```
3. Reinstall the app
4. Check logcat for errors:
   ```bash
   adb logcat | grep Adbify
   ```

## ❓ FAQ

### General Questions

**Q: Do I need root access to use Adbify?**
A: No, root is not required for basic ADB operations. However, some advanced commands on target devices may require root.

**Q: Can I use Adbify to control my phone from itself?**
A: Yes, you can run `adb shell` commands on the device running Adbify, but some operations may be restricted without root.

**Q: Does this work on tablets?**
A: Yes, Adbify works on any Android device (phone or tablet) running Android 7.0 or higher.

**Q: Can I connect to multiple devices simultaneously?**
A: Yes, ADB supports multiple devices. Use `adb -s DEVICE_ID` to specify which device to control.

### Technical Questions

**Q: What ADB version is included?**
A: Adbify includes a custom-compiled ADB binary compatible with modern Android versions. Check version with `adb version`.

**Q: How is ADB running on Android?**
A: Adbify includes native ADB binaries (`libadb.so`) compiled for each architecture, symlinked at runtime to make them executable.

**Q: Where are ADB files stored?**
A: 
- ADB binary: `/data/data/com.adbify/files/bin/adb`
- Home directory: `/data/data/com.adbify/files/home/`
- Temp files: `/data/data/com.adbify/cache/`

**Q: Can I run ADB commands in scripts?**
A: Yes, the terminal supports shell scripts. Create a script and run it with `sh script.sh`.

**Q: How do I update ADB binary?**
A: The ADB binary is bundled with the app. To update, install a newer version of Adbify.

### Troubleshooting Questions

**Q: Why can't I see my device in `adb devices`?**
A: Ensure USB debugging is enabled on target device, USB cable supports data transfer, and permissions are granted.

**Q: Why does the terminal freeze?**
A: Long-running commands may appear frozen. Wait for completion or press Ctrl+C to cancel. Enable "Keep Screen On" to prevent sleep.

**Q: Can I use Adb over the internet?**
A: Not directly. ADB requires same network or USB connection. VPN or port forwarding could enable internet connections but is not recommended for security reasons.

**Q: Why are some characters not displaying correctly?**
A: Ensure you're using a monospace font. Some Unicode characters may not render properly depending on device font support.

**Q: How do I copy text from the terminal?**
A: Long-press on terminal to enter text selection mode, select text, then tap copy icon.

### Development Questions

**Q: Can I contribute to the project?**
A: Yes! Check the GitHub repository for contribution guidelines.

**Q: How do I report bugs?**
A: Open an issue on GitHub with detailed steps to reproduce, device info, and Android version.

**Q: Is the source code available?**
A: Yes, Adbify is open source. Visit: https://github.com/Riz1yotech/ADBify_dev

**Q: What libraries does Adbify use?**
A: Main dependencies include:
- Material Components 1.11.0
- AndroidX Core, Activity, Lifecycle
- Rikka Material components
- Kotlin Coroutines
- Custom terminal emulator module

**Q: Can I modify and redistribute?**
A: Yes, subject to the project's license terms. See [LICENSE](LICENSE) file.

## 📄 License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.

## 🙏 Acknowledgments

- Based on Android Terminal Emulator principles
- Uses Material Design 3 components
- Built with modern Android development practices

## 📞 Support

- **Issues**: https://github.com/Riz1yotech/ADBify_dev/issues
- **Discussions**: https://github.com/Riz1yotech/ADBify_dev/discussions

---

**Made with ❤️ for Android developers and power users**
