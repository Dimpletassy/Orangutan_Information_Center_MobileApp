# OIC IRRIGATION MOBILE APPLICATION
**Project Name:** Orangutan Information Center (OIC) Irrigation Mobile App  
**Developed by:** VC-621 Computing Technology Team, RMIT University  
**Partner Organisation:** Yayasan Orangutan Sumatera Lestari – Orangutan Information Centre (YOSL–OIC)

---

## OVERVIEW
The OIC Irrigation Mobile Application is an Android-based system designed to help farmers and conservation staff manage irrigation schedules efficiently and securely.  
The app connects to a cloud database via Firebase, allowing users to monitor and control automated watering systems from their mobile devices.  
It improves water management, saves resources, and supports sustainable agriculture through manual control, automated scheduling, and real-time reporting.

---

## KEY FEATURES
- **Secure Authentication:** Only authorised users can access system features.
- **Manual Irrigation Control:** Start and stop irrigation manually.
- **Automated Scheduling:** Configure irrigation by day, time, and litres.
- **Real-Time Monitoring:** Displays live irrigation status and remaining litres.
- **Data Logging:** All irrigation runs automatically recorded in Firestore.
- **Reporting Dashboard:** Weekly and monthly water-usage charts.
- **Firebase Integration:** Cloud Firestore for real-time synchronisation and user authentication.

---

## SYSTEM REQUIREMENTS
- Android Studio (latest version)
- Gradle (included with Android Studio)
- Android Emulator (API Level 33 or higher recommended)
- Internet connection
- Firebase account

---

## SETUP INSTRUCTIONS

### ANDROID STUDIO SETUP
1. Download and install **Android Studio**.
2. Download the project source code from GitHub:
    - **Option A (terminal):**
      ```
      git clone https://github.com/Dimpletassy/Orangutan_Information_Center_MobileApp.git
      ```
    - **Option B (ZIP file):**
        - Download the ZIP from GitHub.
        - Extract it into your preferred directory.
3. Open Android Studio → **Open Project**.
4. Select the directory containing the source code.
5. Wait for **Gradle Sync** to complete (this may take several minutes).

---

### ANDROID EMULATOR SETUP
1. In Android Studio, open **Device Manager**.
2. Click the **+** icon → **Create Virtual Device**.
3. Select a phone profile (recommended: *Medium Phone, API 36.0*).
4. Download the system image if necessary.
5. Click **Finish** to create the emulator.

---

### FIREBASE SETUP
1. Go to the [Firebase Console](https://console.firebase.google.com).
2. Create a new Firebase project and follow the prompts.
3. Connect the Android Studio project to Firebase:
    - Tools → Firebase → Authentication → Authenticate using a custom authentication system.
    - Follow **Step 1 only**.
4. Download the Firebase configuration file:
    - In Firebase Console → **Project Settings > General > SDK setup and configuration**.
    - Download **google-services.json**.
    - Place the file in your project’s **app/** directory.
5. Enable **Email/Password Authentication**:
    - Firebase Console → Build → Authentication → Sign-in Method → Enable Email/Password.
6. Create a **Firestore Database**:
    - Firebase Console → Build → Firestore Database → Create Database.
    - Start in **Production Mode**.
    - Update your database rules as follows:

      ```js
      rules_version = '2';
      service cloud.firestore {
        match /databases/{database}/documents {
          match /{document=**} {
            allow read, write: if request.auth != null;
          }
        }
      }
      ```

---

## RUNNING THE APPLICATION
1. Open **Device Manager** and start the Android emulator.
2. In Android Studio, click the **Run (▶)** button next to “app.”
3. The OIC Irrigation App will launch on the emulator.
4. Log in using your Firebase credentials.

---

## NOTES
- You must be logged in to use any of the app’s features.
- Firestore rules restrict access to authenticated users only.
- For testing, temporary relaxation of rules is possible.
- Current version is software-only; future versions can connect to IoT hardware.

