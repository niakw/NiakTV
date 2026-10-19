# Install NiakTV

**English** · [Français](INSTALL.fr.md) · [Back to NiakTV](README.md)

## Before you start

You need an Android TV device running Android 8 or later, a computer with [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools), and a TV that allows APK installation and ADB debugging. Manufacturer settings and Home behaviour vary. The current builds are development APKs.

Nuvio and a TMDB key are optional. Start without either if you just want apps and inputs.

## 1. Get the APK

Open the [Android workflow](https://github.com/niakw/NiakTV/actions/workflows/android.yml), choose a **successful** build and download the **niaktv-debug** artifact. Extract `app-debug.apk` from the ZIP. A failed or still-running build may not have an APK.

## 2. Connect and install

Enable Developer options and ADB debugging on your TV. If the firmware exposes network ADB on port 5555, connect the TV and computer to the same trusted network. Replace `TV_IP` with your own TV address; accept the authorisation prompt on the TV.

```sh
adb connect TV_IP:5555
adb devices
adb -s TV_IP:5555 install -r app-debug.apk
adb -s TV_IP:5555 shell am start -n tv.niak.launcher/.MainActivity
```

The device must appear as `device`, rather than `unauthorized` or `offline`. Some Android TV devices use pairing or a different debug transport; use the connection procedure offered by that device rather than assuming port 5555 is available.

## 3. Make Home feel like home

When Android offers a default Home app chooser, select NiakTV. On some TVs that enforce Google home, **Settings → Home button** opens the accessibility settings: enable **NiakTV — Home button** there, then test the Home key.

This optional service targets the Google Android TV launcher. It is not a universal override for every manufacturer launcher. It does not read window content or require disabling system apps.

## 4. Choose your atmosphere

| Want…                                         | Choose…                                                                                          |
| --------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| Your apps and inputs with fewer content tasks | **Settings → Performance mode**                                                                  |
| A simple backdrop                             | **Background style → Solid** or **Gradient**, then **Palette**                                   |
| Cinema artwork                                | **Background style → Cinema**, then **Now playing · TMDB** to enter a personal key or read token |
| No selection section                          | **My selection section → Hidden**                                                                |
| Another language                              | **Language / Langue → English, Français or TV language**                                         |

Performance mode keeps your previous background and section choices for when you turn it off. Choosing a background or toggling section visibility exits that mode. Changing the palette or language keeps it enabled.

With Nuvio installed, **Nuvio watch next** can request programme access. Without Nuvio, the hero becomes a shortcut to a supported installed streaming app, or disappears when none is found. Other services’ watch history is not imported.

## Updates

Use `adb install -r` with an APK signed by the **same key** as the installed version. CI debug builds and local builds may use different keys, and successive CI builds are not guaranteed a stable signing identity. `INSTALL_FAILED_UPDATE_INCOMPATIBLE` indicates a signature mismatch; recover an APK built with the original key. Uninstalling is not a transparent update: it removes the app’s preferences and saved TMDB credential.

## Return to the original home

Open **Settings → Original home** to stop NiakTV redirection and open Google home. For a lasting return, disable only the **NiakTV — Home button** service in Android accessibility settings and select the original Home app if your TV offers the choice. This action currently targets Google Android TV home; other manufacturer homes may need to be selected in Android settings.

## Troubleshooting

| What you see                        | What to check                                                                                                                                                                          |
| ----------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| ADB connection fails                | Confirm debugging is enabled, the address/transport is correct and the TV has accepted the computer.                                                                                   |
| Home returns to Google              | Recheck NiakTV’s optional accessibility service. Some firmwares clear it after a force-stop.                                                                                           |
| No Nuvio titles                     | Show My selection, leave Performance mode off, confirm Nuvio publishes programmes and allow access through **Nuvio watch next**. An empty feed is not replaced with invented progress. |
| TMDB keeps the old backdrop         | Confirm Cinema is selected, daily change is enabled and the personal credential works. Automatic failed requests are throttled; the previous cached image is retained.                 |
| A TV or HDMI shortcut does not open | Manufacturer integration varies. Use the TV’s input selector and include the affected shortcut in a bug report.                                                                        |

After installation, you can disconnect with `adb disconnect TV_IP:5555` and turn off debugging when you no longer need it.

**Next:** [NiakTV](README.md) · [Accessibility](ACCESSIBILITY.md) · [Report an issue](https://github.com/niakw/NiakTV/issues/new/choose)
