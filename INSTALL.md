# Install NiakTV

**English** · [Français](INSTALL.fr.md) · [Back to NiakTV](README.md)

## Before you start

You need an Android TV device running Android 8 or later that allows APK installation. A computer with [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools) is needed only for the optional ADB method. Manufacturer settings and Home behaviour vary. The current builds are development APKs.

Nuvio and a TMDB key are optional. Start without either if you just want apps and inputs.

## 1. Get the APK

Download **[niaktv-0.3.1.apk](https://github.com/niakw/NiakTV/releases/latest/download/niaktv-0.3.1.apk)** directly from the [latest release](https://github.com/niakw/NiakTV/releases/latest). The signed installer is ready to use; no build tools, GitHub account or archive extraction are required. The release also includes `SHA256SUMS.txt` to check the downloaded file.

## 2. Install on the TV

Transfer the APK to your TV using a USB drive or your preferred file-transfer app. Open it with an Android TV file manager, allow **Install unknown apps** for that source when prompted, and confirm installation. Open NiakTV from your apps. USB and file-manager support depend on your device.

### Alternative: install from a computer with ADB

Enable Developer options and ADB debugging on your TV. If the firmware exposes network ADB on port 5555, connect the TV and computer to the same trusted network. Replace `TV_IP` with your own TV address; accept the authorisation prompt on the TV.

```sh
adb connect TV_IP:5555
adb devices
adb -s TV_IP:5555 install -r niaktv-0.3.1.apk
adb -s TV_IP:5555 shell am start -n tv.niak.launcher/.MainActivity
```

The device must appear as `device`, rather than `unauthorized` or `offline`. Some Android TV devices use pairing or a different debug transport; use the connection procedure offered by that device rather than assuming port 5555 is available.

## 3. Make Home feel like home

When Android offers a default Home app chooser, select NiakTV. On some TVs that enforce Google home, **Settings → Home button** opens the accessibility settings: enable **NiakTV — Home button** there, then test the Home key.

This optional service targets the Google Android TV launcher. It is not a universal override for every manufacturer launcher. It does not read window content or require disabling system apps.

The subtitle under **Settings → Home button** distinguishes native Android Home, a connected compatibility relay, and redirection turned off in NiakTV. If disconnected, open that setting and check the NiakTV service in Android accessibility settings. A service disabled by the system must be enabled again by the user; NiakTV cannot grant itself this permission. Avoid force-stopping the launcher. Google stays installed and the original-home option remains available.

<a id="advanced-home"></a>

### Native Home on firmware that prioritises Google

**Optional advanced setup:** first install and open NiakTV successfully. Disabling the Google launcher changes system Home behaviour and its launcher screens; it retains the package and its data. Make this choice explicitly. The app does not run these commands for you.

```sh
adb -s TV_SERIAL shell am start -n tv.niak.launcher/.MainActivity
adb -s TV_SERIAL shell pm disable-user --user 0 com.google.android.tvlauncher
adb -s TV_SERIAL shell cmd package set-home-activity --user 0 tv.niak.launcher/.MainActivity
adb -s TV_SERIAL shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

Replace `TV_SERIAL` with the device listed by `adb devices`. Confirm the resolver names `tv.niak.launcher/.MainActivity`, then test Home from NiakTV and another app. In this mode Home goes directly to NiakTV and does not need the compatibility relay.

To restore Google, use **Settings → Original home → Open settings → Enable**, then press Home. This turns off NiakTV redirection and opens Google’s Android app settings. As an ADB fallback, run `adb -s TV_SERIAL shell pm default-state --user 0 com.google.android.tvlauncher`, then choose **Original home** in NiakTV. No app data is cleared.

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

Resume artwork uses a private disk cache limited to 64 files and 24 MB. By default, only the three most recent resume items are displayed and prefetched. **Settings → Number of resume items** cycles through **3, 5, 8 or All (up to 32)** on one line. The selected count also limits automatic artwork downloads without changing Nuvio history. Raster images served by NiakTV (artwork and background) use WebP at quality 80; reused legacy files are converted locally. App icons use compact inline SVGs or monograms, with no raster icon images to load. Long-press a My selection shortcut to reorder it within that row; the Apps TV order stays independent. Renditions follow the current order automatically: a banner up to 1,200 × 308 px for the first item, and cards up to 288 × 332 px for the others, cropped before compression without upscaling. Cache keys distinguish the artwork URL and rendition; moving an item generates a missing rendition or reuses an existing one. Uncropped sources and renditions share the same cache limit. Cached artwork is attached before rendering and unchanged cards stay in place on return. The prepared cinema background is reused at startup. Performance mode still disables resume items and their downloads. Android may clear its cache, requiring images to be fetched again.

**My selection app** lets you choose Automatic or an installed app (YouTube, Apple TV, Netflix, etc.). Nuvio resume items are read and prefetched only when Nuvio is the block’s effective app. Other apps display a shortcut. If your chosen app is uninstalled, NiakTV falls back to an installed app, or hides the card when none is available.

## Updates

Use `adb install -r` with an APK signed by the **same key** as the installed version. The published 0.3.1 APK uses the project’s retained local signing key. Use published release APKs for installation; GitHub Actions artifacts are developer checks and may use a different, temporary signing key. `INSTALL_FAILED_UPDATE_INCOMPATIBLE` indicates a signature mismatch; recover an APK built with the original key. Uninstalling is not a transparent update: it removes the app’s preferences and saved TMDB credential.

## Return to the original home

Open **Settings → Original home** to stop NiakTV redirection and open Google home. If Google was disabled using the advanced setup above, NiakTV guides you to its Android settings to choose **Enable** first. For a lasting return, disable only the **NiakTV — Home button** service in Android accessibility settings and select the original Home app if your TV offers the choice. This action currently targets Google Android TV home; other manufacturer homes may need to be selected in Android settings.

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
