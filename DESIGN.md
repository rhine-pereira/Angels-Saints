# Design: Kurus at the Village Gate

The app is the painted plaque at a Vasai wayside cross (*kurus*): limewashed ground, indigo trim, a fresh marigold garland for whatever is live today, and an oil lamp at night. Material 3 provides structure and controls; identity lives in colour roles, type, and a few hand-drawn devices.

## Colour

Strategy: restrained. Neutral limewash/indigo carries every screen. Marigold appears only for "today/live". The liturgical season colour appears as trim, never as a fill.

| Role | Light | Dark (lamp-lit) |
|---|---|---|
| background / surface | Limewash `#F3F5F2` | Indigo night `#121A33` |
| onSurface (ink) | `#1B2547` | Lamp white `#ECE6D6` |
| primary (indigo trim) | `#22305C` | `#C9D3F5` |
| secondary (marigold ink, text-safe) | `#7A4D00` | `#FFB940` |
| secondaryContainer (marigold garland) | `#F2A007` | `#F2A007` |
| tertiary / rubric (laterite) | `#8A3B24` | `#F0A58C` |
| outline / outlineVariant | `#5A6382` / `#C5CAD6` | `#8890AE` / `#343E62` |

Vestment colours (`Kurus.colors.vestment()`): green `#2F6B3F`, violet `#5B2A6E`, white→gold-cream `#D8C68A`, red `#A3241C`, rose `#C9607F`, black, gold. Each has a brighter dark-theme twin. Dynamic colour is off on purpose.

Rules:
- Marigold is never used as text on limewash; use `secondary` for text.
- Rubrics (Response, Acclamation) use laterite, not `error`.

## Type

- **Yatra One** (signpainted, Latin + Devanagari): `display*` and `headlineLarge/Medium`. Used for the day numeral, feast title, screen titles and the wheel centre.
- **Mukta**: all UI roles (headlineSmall → label).
- **Martel**: `ReadingText` (18/32sp) for Scripture, prayers and the Catechism.
- All sizes are in sp. Body and reading text never use Yatra.

## Shape and line

- Plaques are rectangles with a 4dp radius (`extraSmall`) and a 1.5dp indigo trim border; there is no elevation.
- `TrimRule` is the divider. Indigo marks sections; `outlineVariant` separates list rows.
- Art thumbnails use a 6dp radius at a portrait 1:1.3 ratio.
- There are no drop-shadow cards, gradients or scrims over text.

## Signature devices

- **Day numeral** with the neighbouring days ghosted (18% alpha, tappable) and a marigold garland bar under today only.
- **SeasonTrim**: each season has its own line pattern, so colour is never the only signal. Advent is dashed, Christmas a double line, Lent dotted, the Triduum thick, Easter long-dashed, and Ordinary Time solid.
- **Year wheel**:
  - The ring starts at Jan 1 at twelve o'clock and runs clockwise, with season arcs and month ticks.
  - Today is marked by a marigold tick.
  - The indigo knob is the only drag handle, so page scroll stays free.
  - Tapping an arc opens the season explainer sheet.
- **Reading plates**: Yatra roman numeral in the gutter, then kind, reference (marigold ink), and Martel text. Verse numbers are small marigold-ink figures.

## Layout

- 24dp horizontal page margin.
- Lists are flat rows separated by rules; no cards.
- Navigation: an M3 NavigationBar (Today / Library / Catechism) with a marigold indicator. Detail screens push on top and use the same `KurusTopBar` with a back arrow.
- Empty, offline and error states share `MessageState`: a short trim rule, a Yatra title, and a calm body.
