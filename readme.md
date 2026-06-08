# Screen Dimmer Pro

**Screen Dimmer Pro** (developed by Akila Madhushanka of **Nextgenware Software Solutions**) is a professional-grade utility designed to provide advanced, software-based brightness control for Android devices.

## Project Purpose
The primary goal of this application is to solve common hardware limitations and accessibility issues related to screen brightness. Many Android devices have a "minimum brightness" that is still too intense for comfortable use in pitch-black environments, leading to eye strain and disrupted sleep. Additionally, users with malfunctioning hardware brightness sensors or broken physical controls often find themselves unable to adjust their screen comfortably.

## How it Works
Unlike standard system setThe primary goal of this application is to solve common hardware limitings that adjust the backlight hardware, **Screen Dimmer Pro** utilizes a sophisticated **Overlay Rendering Engine**. It creates a system-wide, touch-transparent black layer using the Android `WindowManager`. By adjusting the opacity of this layer, the application effectively "dims" the screen beyond the system's hardware limits, providing a customized viewing experience that is compatible with all apps.

## Key Features
*   **Precision Dimming:** A 0% to 90% dimming range that allows users to find the perfect level for night reading or low-light usage.
*   **Persistent Control:** Utilizes a **Foreground Service** to ensure the dimming remains active even when the system is under heavy memory pressure.
*   **Integrated Controls:** 
    *   **Notification Center:** A VLC-style controller built into the notification shade for instant adjustments without leaving your current app.
    *   **Quick Settings Tile:** Integration with the Android status bar for one-tap activation.
*   **System Resilience:** Features **Auto Startup** via a boot receiver, ensuring your preferred brightness settings are restored immediately after a device reboot.
*   **Modern Architecture:** Built using **Kotlin** and **Jetpack Compose**, following **MVVM** patterns for a lightweight, battery-efficient, and responsive user experience.

## Target Audience
Screen Dimmer Pro is ideal for night readers, users with light sensitivity, and individuals with devices suffering from faulty brightness hardware. It transforms the screen into a more eye-friendly interface, enhancing comfort and usability during late-night or low-light sessions.

---
© 2024 Nextgenware Software Solutions. All rights reserved.
