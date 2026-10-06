# MYRAA Android build

This build preserves the MYRAA React UI and visual/3D presentation while removing the assumption that Electron/Node desktop APIs exist inside the APK.

## Android fixes
- Relative Vite assets and Android WebView asset loader.
- Responsive phone layout at narrow widths; no forced 360px/desktop sidebars.
- Local Android settings/memory fallback so missing PC `/api/*` endpoints do not blank the UI.
- No fake `Holographic network link lost` error when the APK has no desktop `/live` WebSocket backend configured.
- Android permissions and accessibility/notification/overlay services remain available.

## Build
GitHub Actions runs `npm run build:android`, copies `dist/` into the Android assets, then builds `app-debug.apk`.

The original Electron/Windows automation backend is not bundled into the APK because it cannot run as an Android process.
