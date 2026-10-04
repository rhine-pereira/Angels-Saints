Generates `app/src/main/assets/calendar/{2025..2035}.json` + `saints.json` (India liturgical calendar, English) using romcal v3 + @romcal/calendar.india.
Re-run: `cd tools/calendar-gen && npm install && node generate.mjs && node check.mjs`
To change the year range, edit the `for (let y = 2025; y <= 2035; y++)` loop in generate.mjs (and check.mjs).
India-specific fixes vs romcal data (Gonsalo Garcia Feb 7, Devasahayam Jan 14, Sunday transfers) are at the top of generate.mjs.
