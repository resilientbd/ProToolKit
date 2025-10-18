# Signing Configuration

This project uses the standard Android debug keystore for both debug and release builds to simplify development and testing.

## Keystore Properties

The signing configuration is defined in `keystore.properties`:

```properties
storePassword=android
keyPassword=android
keyAlias=androiddebugkey
storeFile=/Users/skfaisal/.android/debug.keystore
```

## Build Variants

Both build variants are configured with signing:

1. **Debug Variant**: Uses the debug keystore with debug signing config
2. **Release Variant**: Uses the debug keystore with release signing config

## Building APKs

To build both variants:

```bash
# Build debug APK
./gradlew app:assembleDebug

# Build release APK
./gradlew app:assembleRelease

# Build both variants
./gradlew app:assemble
```

## Output Location

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

## Production Signing

For production releases, you should create a dedicated release keystore and update the `keystore.properties` file accordingly.