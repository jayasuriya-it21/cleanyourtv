# CleanYourTV Provider Capability Matrix

This matrix documents the integration capabilities, verified behavior, and explicit status of external applications and streaming providers for the CleanYourTV Content Hub (`com.gathin.tvhome`) targeting the TCL 55T6D (Google TV, India region).

> [!IMPORTANT]
> **Physical Hardware Gate**: The target TCL 55T6D has not yet been connected over network ADB. Zero provider-specific deep links or in-app intents are marked as verified. All capabilities default to `NOT_IMPLEMENTED` or `IMPLEMENTED_UNTESTED` (generic package launches only).

---

## Explicit Capability States

Each provider capability is evaluated and tracked under one of the following authoritative states:
- **`NOT_IMPLEMENTED`**: No intent builder or adapter code exists yet.
- **`IMPLEMENTED_UNTESTED`**: Code exists (e.g. generic package intent) but has not been resolved or executed on the target device.
- **`RESOLVES_ON_DEVICE`**: Intent queries resolve against an installed package via `PackageManager.resolveActivity()`, but launch and playback have not been validated.
- **`VERIFIED_ON_DEVICE`**: End-to-end launch, parameter passing, playback, or focus behavior has been tested and confirmed on the physical TV.
- **`UNSUPPORTED`**: The provider app or platform does not support this integration path.
- **`BLOCKED_BY_PLATFORM`**: Restricted by Android TV OS permissions, OEM signature protection, or DRM boundaries.

---

## Provider Capability Matrix

| Provider | Target Package(s) | Generic App Launch | Provider Adapter | Search | Open Title | Playback | Playback Progress | Resolution State | Physical Verification | Fallback Behavior | Candidate Schemes & Integration Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Netflix** | `com.netflix.ninja` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Candidate scheme: `nflx://www.netflix.com/title/{provider_id}`. TMDB IDs must NOT be passed. Requires provider-specific ID lookup. |
| **Amazon Prime Video** | `com.amazon.amazonvideo.livingroom` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Candidate URI: `https://app.primevideo.com/detail?gti={asin}`. ASIN is Amazon-specific. Deep link unverified on TCL TV. |
| **Disney+ Hotstar / JioHotstar** | `in.startv.hotstar` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Candidate URI: `hotstar://content/{content_id}`. Hotstar ID mapping required. Deep link unverified. |
| **SunNXT** | `com.suntv.sunnxt` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Regional Tamil/Telugu provider. Deep linking scheme undocumented; defaults strictly to `OPEN_APP`. |
| **Zee5** | `com.graymatrix.did` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Candidate URI: `zee5://content/{zee5_id}`. Deep link unverified. |
| **SonyLIV** | `com.sonyliv` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Candidate intent unverified. Defaults to package launch. |
| **Aha Tamil / Aha** | `vikatan.aha.video`, `com.aha.aha` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Regional Tamil provider. Defaults strictly to `OPEN_APP`. |
| **YouTube** | `com.google.android.youtube.tv` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Candidate URI: `vnd.youtube://{video_id}`. Unverified until physical hardware connection. |
| **VLC for Android** | `org.videolan.vlc` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Generic `ACTION_VIEW` video candidate. No progress contract; recorded as Recently Opened only. |
| **Kodi** | `org.xbmc.kodi` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Standalone media suite. No intent result contract; recorded as Recently Opened only. |
| **Jellyfin Android TV** | `org.jellyfin.androidtv` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Generic package launch inherited from app discovery. Authenticated server API integration planned for Milestone 4. |
| **Plex for Android TV** | `com.plexapp.android` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Generic package launch inherited from app discovery. Client resolution check pending TV probe. |
| **Nova Video Player** | `org.courville.nova` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Generic `ACTION_VIEW` candidate. No progress contract; recorded as Recently Opened only. |
| **MX Player TV** | `com.mxtech.videoplayer.television`, `com.mxtech.videoplayer.ad` | `INHERITED_LAUNCHABLE` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` | `NOT_IMPLEMENTED` (Recently Opened only) | Untested | Untested | Fallback to `OPEN_APP` | Video player candidate. Recorded as Recently Opened only. |

---

## Playback-Progress and Continue Watching Policy

> [!CAUTION]
> **Strict Policy on Playback Progress**:
> CleanYourTV **does not** claim true playback progress (playback position, duration, or resume state) for external OTT applications or third-party players (VLC, Kodi, Nova, MX Player) launched via `ACTION_VIEW` or `OPEN_APP`.
>
> 1. Launching an external application or title registers the item **only** in the launcher's local **"Recently Opened"** list with a local timestamp.
> 2. `CONTINUE_WATCHING` progress bars and resume offsets are strictly restricted to:
>    - Playback occurring directly inside a launcher-owned Media3 player, OR
>    - Documented, authenticated server APIs (e.g. Jellyfin server sync) returning real session progress, OR
>    - A verified Android `startActivityForResult` contract returning standardized playback position extras (must be empirically proven on the TV).

---

## Content ID Disambiguation Policy

> [!WARNING]
> **Zero ID Conflation**:
> Metadata IDs (such as TMDB numeric IDs, e.g. `550`) are strictly canonical metadata keys and must **never** be substituted into an external OTT provider's URI scheme (e.g. `nflx://.../550` or `hotstar://.../550`).
>
> A deep link may only be constructed when:
> 1. The metadata source legally supplies a verified external ID mapping for that provider, OR
> 2. The provider's deep linking specification is verified on physical hardware to accept canonical search queries, OR
> 3. The launcher safely falls back to provider in-app search or top-level app launch.
