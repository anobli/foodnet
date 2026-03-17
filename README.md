# FoodNet

An Android application for food sharing and tracking built with Firebase.

## Prerequisites

Before building the project, ensure you have the following installed:

- **JDK 17** or higher
- **Android Studio** (latest stable version recommended)
- **Android SDK** with the following:
  - Android SDK Platform 34 (compile SDK)
  - Android SDK Platform 33 (target SDK)
  - Android SDK Build-Tools 34.0.0 or higher

## Project Info

- **Application ID**: `ovh.bailon.foodnet2`
- **Min SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 33 (Android 13)
- **Compile SDK**: 34 (Android 14)
- **Version**: 1.03 (versionCode: 4)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/anobli/foodnet.git
cd foodnet
```

### 2. Configure Firebase

You need to set up Firebase for this project:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use an existing one
3. Add an Android app with package name: `ovh.bailon.foodnet2`
4. Download the `google-services.json` file
5. Place it in the `app/` directory:
   ```bash
   cp /path/to/your/google-services.json app/google-services.json
   ```

### 3. Configure Google Ads (Optional for Debug Builds)

For **debug builds**, the app uses test Ad IDs automatically, so this step is optional.

For **release builds**, create a `google_ads.properties` file in the project root:

```properties
# google_ads.properties
MOBILEADS_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
BANNER_AD_MAIN=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
BANNER_AD_LIST=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
```

Replace the X's with your actual Google AdMob IDs from [AdMob Console](https://apps.admob.com/).

## Building the App

### Build Debug APK (For Testing)

This is the easiest way to build and test the app:

```bash
./gradlew assembleDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

You can install it directly on your device:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK

For release builds, you need a signing keystore. If you don't have one, create it:

```bash
keytool -genkey -v -keystore your_keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias your_alias
```

Then set environment variables:
```bash
export SIGNING_STORE_PASSWORD="your_store_password"
export SIGNING_KEY_ALIAS="your_alias"
export SIGNING_KEY_PASSWORD="your_key_password"
```

Update the keystore path in `app/build.gradle` (line 19):
```gradle
storeFile = file("../path/to/your_keystore.jks")
```

Build the release APK:
```bash
./gradlew assembleRelease
```

The signed APK will be at:
```
app/build/outputs/apk/release/app-release.apk
```

### Build App Bundle (AAB)

To build an App Bundle:

```bash
./gradlew bundleRelease
```

The bundle will be at:
```
app/build/outputs/bundle/release/app-release.aab
```

## Using Android Studio

1. Open Android Studio
2. Select **File > Open** and choose the project directory
3. Wait for Gradle sync to complete
4. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)** or **Build Bundle(s)**

Or use the run button to build and install directly on a connected device/emulator.

## Configuration Files Summary

| File | Location | Required For | Status |
|------|----------|--------------|--------|
| `google-services.json` | `app/` | All builds | **Required** |
| `google_ads.properties` | Root directory | Release builds only | Optional for debug |
| Signing keystore | Custom path | Release builds only | Required for release |

## Troubleshooting

### Build Fails with "google-services.json not found"
- Ensure you've placed the Firebase config file in the `app/` directory
- Check that the file is named exactly `google-services.json`

### Gradle Sync Issues
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### JDK Version Issues
Ensure you're using JDK 17. Check with:
```bash
java -version
```

Set JAVA_HOME if needed:
```bash
export JAVA_HOME=/path/to/jdk-17
```

### Permission Issues on Linux/Mac
```bash
chmod +x gradlew
```

## Dependencies

Key dependencies used:
- Firebase Authentication & Firestore
- Google Play Services Ads
- AndroidX libraries
- ZXing (QR code scanning)
- Material Design Components

## CI/CD

The project includes a GitHub Actions workflow (`.github/workflows/android.yml`) that automatically:
- Builds debug and release APKs
- Generates App Bundle (AAB)
- Runs on push to main branch

## License

See [LICENSE](LICENSE) file for details.
