# ProToolKit - Professional Device Utilities Suite

ProToolKit is a comprehensive Android application that provides professional-grade device utilities including network diagnostics, system information, file management, and more.

## Features

### 1. Network Tools
- **Latency Testing**: Measure ping times to any server
- **Speed Testing**: Test download and upload speeds
- **Connection Checking**: Verify network connectivity
- **Host Pinging**: Check if specific hosts are reachable

### 2. Device Information
- **Comprehensive Specs**: Manufacturer, model, brand details
- **Hardware Info**: Board, bootloader, hardware specifics
- **Storage Analytics**: Total, free, and categorized storage
- **Memory Statistics**: RAM usage and thresholds
- **Network Details**: Type, carrier, IP/MAC addresses
- **Screen Specs**: Size, resolution, density
- **CPU Information**: Model, cores, architecture
- **System Capabilities**: Supported ABIs, OpenGL ES

### 3. File Management
- **Storage Overview**: Visual breakdown of storage usage
- **Detailed Analysis**: Apps, images, videos, audio, documents, downloads
- **Quick Actions**: Clean cache, clear downloads, backup media
- **Smart Suggestions**: Contextual recommendations based on usage

### 4. QR & Barcode Scanning
- **Multi-format Support**: QR codes, barcodes, and more
- **Smart Handling**: Different actions based on content type
- **WiFi Setup**: One-tap WiFi network configuration
- **Web Links**: Direct browser opening
- **Contact Info**: Easy contact saving

### 5. Unit Conversion
- **Multiple Categories**: Length, weight, temperature, etc.
- **Precision Calculations**: Accurate conversions
- **Popular Units**: Comprehensive unit coverage

## Monetization & In-App Purchases

### Setup AdMob
1. **Create AdMob Account**: Go to [AdMob Console](https://apps.admob.com/)
2. **Create App**: Register your app and get the App ID
3. **Create Ad Units**: Create banner, interstitial, and rewarded ad units
4. **Update Configuration**:
   - Open `app/src/main/res/values/strings.xml`
   - Replace `YOUR_ADMOB_APP_ID` with your actual App ID
   - Replace ad unit IDs with your actual unit IDs:
     ```xml
     <string name="admob_app_id">ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy</string>
     <string name="admob_banner_id">ca-app-pub-3940256099942544/6300978111</string>
     <string name="admob_interstitial_id">ca-app-pub-3940256099942544/1033173712</string>
     <string name="admob_rewarded_id">ca-app-pub-3940256099942544/5224354917</string>
     ```

### Setup In-App Purchases
1. **Google Play Console**:
   - Create your app in Google Play Console
   - Navigate to "Monetize" > "Products" > "In-app products"
   - Create the following products:
     - `protoolkit_premium` - Premium features
     - `protoolkit_remove_ads` - Remove advertisements
     - `protoolkit_unlock_all` - Unlock all features

2. **Configure Products**:
   - Set appropriate titles, descriptions, and prices
   - Publish the products

3. **Update Billing Configuration**:
   - The app uses the following product IDs:
     - Premium: `protoolkit_premium`
     - Remove Ads: `protoolkit_remove_ads`
     - Unlock All: `protoolkit_unlock_all`

### Developer Mode & Sandbox Testing

#### Enabling Developer Mode
1. **Access Developer Settings**:
   - Go to Settings > About
   - Tap "Version" 7 times to enable Developer Options

2. **Developer Features**:
   - Sandbox mode for testing purchases
   - Extended logging
   - Debug information display

#### Testing In-App Purchases
1. **Sandbox Mode**:
   - Enable sandbox mode in Developer Settings
   - Use test product IDs:
     - `android.test.purchased` - Successful purchase
     - `android.test.canceled` - Cancelled purchase
     - `android.test.item_unavailable` - Unavailable item

2. **License Testing**:
   - Add test accounts in Google Play Console
   - Use licensed testers for real purchase testing

### Development Configuration

#### Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/protoolkit/
│   │   │       ├── billing/           # Billing management
│   │   │       ├── data/              # Data layer
│   │   │       │   ├── billing/       # Billing repository
│   │   │       │   ├── device/        # Device info repository
│   │   │       │   ├── file/          # File tools repository
│   │   │       │   ├── network/       # Network tools repository
│   │   │       │   └── settings/      # Settings repository
│   │   │       ├── domain/           # Domain models
│   │   │       ├── ui/                # UI layer
│   │   │       │   ├── base/          # Base classes
│   │   │       │   ├── home/          # Home screen
│   │   │       │   ├── settings/      # Settings screen
│   │   │       │   └── tools/         # Tools screens
│   │   │       │       ├── device/    # Device info tool
│   │   │       │       ├── file/      # File tools
│   │   │       │       ├── network/   # Network tools
│   │   │       │       └── qr/        # QR scanner tool
│   │   │       └── util/              # Utility classes
│   │   └── res/                       # Resources
│   └── debug/
└── build.gradle                       # App build configuration
```

#### Key Classes

##### BillingManager
Manages all in-app purchase operations:
- Connection to Google Play Billing
- Product querying
- Purchase flow handling
- Purchase acknowledgment

##### DeveloperModeUtil
Handles developer and sandbox configurations:
- Toggle sandbox mode
- Manage debug logging
- Configure testing environments

##### AdsManager
Manages advertisement display:
- Banner ads
- Interstitial ads
- Rewarded ads

## Building & Deployment

### Prerequisites
- Android Studio Flamingo or later
- Android SDK API 34+
- Google Services Plugin
- Google Play Billing Library

### Build Configuration
```gradle
// app/build.gradle
dependencies {
    implementation "com.android.billingclient:billing:6.0.1"
    implementation "com.google.android.gms:play-services-ads:22.5.0"
}
```

### Signing Release
1. **Generate Keystore**:
   ```bash
   keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Configure signingConfigs**:
   ```gradle
   android {
       signingConfigs {
           release {
               storeFile file('my-release-key.keystore')
               storePassword 'password'
               keyAlias 'alias_name'
               keyPassword 'password'
           }
       }
   }
   ```

## Testing

### Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Instrumentation Tests
```bash
./gradlew connectedDebugAndroidTest
```

## Troubleshooting

### Common Issues

1. **Billing Not Working**:
   - Ensure test account is added to Google Play Console
   - Check product IDs match exactly
   - Verify app is published (even in draft)

2. **Ads Not Loading**:
   - Confirm AdMob IDs are correct
   - Check internet permissions
   - Ensure proper initialization

3. **Developer Mode Issues**:
   - Clear app data after toggling modes
   - Restart the app after configuration changes

### Logs & Debugging
Enable verbose logging in Developer Settings for detailed debugging information.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Support
For support, please open an issue on the GitHub repository or contact the development team.