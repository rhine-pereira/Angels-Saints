# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users
English- and Marathi-speaking Catholics in India (Maharashtrian / Vasai-Mumbai community), on the Play Store. Primary use is daily: checking the day's Mass readings and feast, often in the morning. Secondary use is occasional reference: looking up a saint, angel, apostle, or prayer, or a Catechism paragraph.

## Product Purpose
A Catholic digital library centered on the liturgical day. Success = a user opens the app each day and gets today's readings and feast in one glance, in their language, even offline.

## Positioning
Bilingual (English + Marathi) daily readings and devotional content for Indian Catholics, in one offline-capable app, alongside a full searchable Catechism bundled on-device.

## Operating Context
- Daily ritual: morning readings, feast of the day; FCM push on topic `daily_readings` deep-links to `saints://daily-readings`.
- Content is static JSON on GitHub Pages (`rhine-pereira.github.io/angelsandsaints_data`), cached aggressively by OkHttp for offline use; CCC ships in `assets/ccc.json`.
- Phones, Android 7+ (minSdk 24, targetSdk 36), varied network quality.

## Capabilities and Constraints
- Sections: Daily Feast, Daily Readings (English + Marathi), Catechism (searchable), library categories (angels, saints, child saints, apostles, prayers). Content items can carry language `versions`.
- Language: Marathi is an app-wide remembered preference with per-screen override.
- Dates: no hardcoded date bounds; a date with no published data shows a graceful "not published yet" state.
- Stack: Jetpack Compose + Material 3, Navigation Compose, Retrofit/Moshi, Coil, Firebase Messaging. No DI framework or database by decision; Compose BOM / AndroidX may be bumped.
- Navigation: bottom nav (Today, Library, Catechism; Saved later), replacing the tile hub.

## Brand Commitments
Name "Angels & Saints". Tone must be reverent. Existing burgundy/gold look is not binding; a redesign is approved.

## Evidence on Hand
Real content lives in the remote JSON repo and `app/src/main/assets/ccc.json`. Category art in `app/src/main/res/drawable/`. No testimonials, ratings, or user counts exist; do not fabricate them.

## Product Principles
1. Today first: the liturgical day is the front door; the library is one tap away, never in the way.
2. Language is respected, not toggled: Marathi readers should never feel second-class.
3. Works offline, fails gracefully: missing or unpublished content is a calm state, never an error wall.
4. Reverent, not kitsch: the content carries the weight; the interface stays quiet and legible.

## Accessibility & Inclusion
Long-form reading in two scripts (Latin, Devanagari): text must scale with system font size and both scripts need proper line height and font coverage.
