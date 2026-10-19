<div align="center">
  <img src="assets/branding/niaktv-logo.svg" alt="NiakTV — Your TV. Your space." width="800">
  <p><strong>English</strong> · <a href="README.fr.md">Français</a></p>
  <h3>A home screen that feels like yours.</h3>
  <p><kbd>Android TV 8+</kbd> <kbd>No ads</kbd> <kbd>Cinema or minimal</kbd> <kbd>EN / FR</kbd></p>
  <p>Your apps. Your inputs. Your next watch.<br>A considered TV interface, with the freedom to keep only what you need.</p>
</div>

<p align="center"><a href="#-make-yourself-at-home"><strong>Experience</strong></a> · <a href="#-install-in-a-few-minutes"><strong>Install</strong></a> · <a href="#-make-it-yours"><strong>Customise</strong></a> · <a href="VALIDATION.md"><strong>Development & validation</strong></a></p>

---

## ✨ Make yourself at home

| Your home                      | Your choice                                                                                                                                                                                            |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **A cinema atmosphere**        | Daily TMDB backdrops from now-playing films, with the film’s original title discreetly credited at the bottom left. Use your own API key.                                                              |
| **A calmer alternative**       | Choose a solid colour or static gradient in Mint, Violet, Blue or Graphite. No wallpaper downloads in these modes.                                                                                     |
| **Your next watch**            | Real watch-next titles published by Nuvio, with artwork and progress when available. Without Nuvio, an installed supported streaming app becomes a shortcut; without one, no replacement card appears. |
| **Only what matters**          | Hide **My selection** entirely. **Performance mode** also switches to a solid background, stops watch-next/image loading and simplifies focus effects.                                                 |
| **A proper remote experience** | Clear mint/violet focus, directional navigation, custom app marks, favourites and a dock you can reorder. Hold OK on an app to organise it.                                                            |
| **Your language**              | Full English and French interface. Follow the TV language or choose one in Settings. App names and programme titles retain their original labels.                                                      |

**My selection → Inputs → TV apps.** No sponsored rows, autoplay trailers or telemetry. The interface and fonts live on the TV. Optional content downloads happen while the launcher is in the foreground.

## ⚡ Install in a few minutes

1. Download the **niaktv-debug** APK artifact from a successful [Android workflow run](https://github.com/niakw/niaktv/actions/workflows/android.yml) and extract `app-debug.apk`. These are development builds.
2. On the TV, enable Developer options and ADB debugging. Connect your computer and TV to the same network and accept the TV’s debugging prompt.
3. With [Android Platform Tools](https://developer.android.com/tools/releases/platform-tools) installed, replace `TV_IP` below with your TV’s address:

```sh
adb connect TV_IP:5555
adb -s TV_IP:5555 install -r app-debug.apk
adb -s TV_IP:5555 shell am start -n tv.niak.launcher/.MainActivity
```

4. Open **Settings → Home button**. Choose NiakTV as the Home app when the TV allows it. On compatible TVs that enforce Google home, enable the optional **NiakTV — Home button** accessibility service.

**Keep a way back:** **Settings → Original home** stops redirection and opens Google home. You can also turn off NiakTV’s accessibility service in Android settings. No system app needs to be removed or disabled. Manufacturer behaviour varies.

Updates must use the same signing key as the installed APK. CI artifacts and locally signed builds may have different keys; Android will reject an incompatible update.

## 🎛 Make it yours

- **Cinema:** Settings → Background style → Cinema, then configure your personal TMDB API key or read access token. Enable daily change or keep the current backdrop. **Next wallpaper** requests another film.
- **Minimal:** switch on **Performance mode** for inputs and apps on a solid background. Switching it off restores your previous selection/background choices.
- **Your balance:** hide **My selection** independently, or choose a gradient without giving up favourites. Pick your accent palette and language.
- **Watch next:** with Nuvio installed, allow Android TV programme access when prompted. NiakTV can only show titles Nuvio publishes. Opening a title follows Nuvio’s own link and may show its source chooser.

Your TMDB credential stays in the app’s private Android storage, excluded from backups. It is never embedded in the APK, sent to the JavaScript interface or committed to this repository. **No TMDB account or Nuvio installation is required to use the launcher.**

## 🛠 Built to stay small

Local HTML/CSS/JavaScript with a native Android bridge. No web framework, remote page, ad SDK or analytics. Bounded image cache, deferred artwork and foreground-only refreshes. The optional Home accessibility service observes only Google home window changes, with no access to window content.

Performance mode reduces work; it is not a system-wide Android optimisation or a guarantee of 60 fps. See [validation, build instructions and known limits](VALIDATION.md).

## Credits

This product uses the TMDB API but is not endorsed or certified by TMDB. Film artwork belongs to its respective rights holders. NiakTV is independent of TMDB, Nuvio and the application brands shown in its interface.

Manrope — Mikhail Sharanda, Mirko Velimirovic. Instrument Serif — Instrument. Both fonts are bundled under the SIL Open Font License; licence files are included alongside the fonts. The fallback cinema artwork was created for NiakTV.
