// Generates app/src/main/assets/calendar/{YYYY.json,saints.json} from romcal (India, English).
import { Romcal } from 'romcal';
import { India_En } from '@romcal/calendar.india';
import { mkdirSync, writeFileSync } from 'node:fs';

const OUT = new URL('../../app/src/main/assets/calendar/', import.meta.url);
mkdirSync(OUT, { recursive: true });

// Fixes to romcal's India data to match gcatholic.org/calendar/2026/IN-en:
// Gonsalo Garcia is Feb 7 in India (romcal puts it on Feb 6 where Paul Miki suppresses it).
India_En.inputs.gundisalvus_garcia_martyr[0].dateDef = { month: 2, date: 7 };
India_En.i18n.names.gundisalvus_garcia_martyr = 'Saint Gonsalo Garcia, Religious and Martyr';
// Devasahayam (canonised 2022) is missing from romcal.
India_En.inputs.devasahayam_pillai_martyr = [{ dateDef: { month: 1, date: 14 }, precedence: 'OPTIONAL_MEMORIAL_12', colors: ['RED'], fromCalendarId: 'india' }];
India_En.martyrology.devasahayam_pillai_martyr = { canonizationLevel: 'SAINT', titles: ['MARTYR'] };
India_En.i18n.names.devasahayam_pillai_martyr = 'Saint Devasahayam, Martyr';

// India: Epiphany, Ascension and Corpus Christi are transferred to Sunday (verified on gcatholic).
const romcal = new Romcal({ localizedCalendar: India_En, epiphanyOnSunday: true, ascensionOnSunday: true, corpusChristiOnSunday: true });

const SEASON = { ADVENT: 'ADVENT', CHRISTMAS_TIME: 'CHRISTMAS', LENT: 'LENT', ORDINARY_TIME: 'ORDINARY_TIME' };
// Holy Saturday has no colour in romcal (no Mass); PURPLE is a fallback.
const cap = (s) => s.charAt(0).toUpperCase() + s.slice(1);
const all = {};
for (let y = 2025; y <= 2035; y++) {
  const cal = await romcal.generateCalendar(y);
  const days = Object.keys(cal).sort().map((d) => {
    const [p, ...rest] = cal[d];
    const ss = p.calendar.seasons;
    const s = ss.includes('EASTER_TIME') ? 'EASTER' : ss.includes('PASCHAL_TRIDUUM') ? 'PASCHAL_TRIDUUM' : SEASON[ss[0]];
    const e = { d, t: cap(p.name), r: p.rank, c: p.colors[0] ?? 'PURPLE', s, w: p.calendar.weekOfSeason ?? 0 };
    if (rest.length) e.o = rest.map((x) => cap(x.name));
    return e;
  });
  writeFileSync(new URL(`${y}.json`, OUT), JSON.stringify(days));
  all[y] = cal;
}

// saints.json: fixed-date sanctorale. 2026 first, then other years to catch memorials suppressed in 2026 (e.g. on Sundays).
const TITLE_WORDS = new Set('pope priest priests bishop bishops martyr martyrs virgin religious abbot abbess deacon monk hermit evangelist apostle apostles archangels doctor doctors of the church and first'.split(' '));
const isTitle = (seg) => /^(Spouse|Parents) of /.test(seg) || seg.split(' ').every((w) => TITLE_WORDS.has(w.toLowerCase()));
// 'Saints Cyril, Monk, and Methodius, Bishop' -> 'Cyril and Methodius'; 'Saint Joseph, Spouse of ...' -> 'Joseph'
function bareName(t) {
  if (/Dedication/.test(t)) return null;
  const m = t.match(/^(?:Saints?|Blessed) (.+)$/) ?? t.match(/ of Saints? (.+?)(?: the Apostle)?$/);
  if (m) return m[1].split(', ').filter((seg) => !isTitle(seg)).reduce((a, seg) => a + (seg.startsWith('and ') ? ' ' : ', ') + seg);
  return /Mary|Blessed Virgin|Our Lady/.test(t) ? 'Mary' : null;
}
// "Fixed date" = the celebration lands on its most common MM-DD in at least half the years it appears
// (tolerates occasional transfers like Joseph/Annunciation in Holy Week; rejects moveable feasts).
const seen = new Map(); // title -> Map(MM-DD -> count)
for (const cal of Object.values(all)) for (const items of Object.values(cal)) for (const x of items) {
  const t = cap(x.name);
  if (!bareName(t)) continue;
  const dates = seen.get(t) ?? seen.set(t, new Map()).get(t);
  const md = x.date.slice(5);
  dates.set(md, (dates.get(md) ?? 0) + 1);
}
const list = [];
for (const [t, dates] of seen) {
  const total = [...dates.values()].reduce((a, b) => a + b);
  const [md, count] = [...dates].sort((a, b) => b[1] - a[1])[0];
  if (count * 2 < total) continue;
  const [m, day] = md.split('-').map(Number);
  list.push({ n: bareName(t), t, m, day });
}
list.sort((a, b) => a.m - b.m || a.day - b.day);
writeFileSync(new URL('saints.json', OUT), JSON.stringify(list));
console.log(`wrote 11 years + ${list.length} saints`);
