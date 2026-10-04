import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
const load = (f) => JSON.parse(readFileSync(new URL(`../../app/src/main/assets/calendar/${f}`, import.meta.url)));
for (let y = 2025; y <= 2035; y++) {
  const leap = (y % 4 === 0 && y % 100 !== 0) || y % 400 === 0;
  assert.equal(load(`${y}.json`).length, leap ? 366 : 365, `${y} length`);
}
const day = Object.fromEntries(load('2026.json').map((e) => [e.d, e]));
assert.deepEqual([day['2026-04-05'].r, day['2026-04-05'].c, day['2026-04-05'].s], ['SOLEMNITY', 'WHITE', 'EASTER']);
assert.match(day['2026-12-25'].t, /Nativity of the Lord|Christmas/);
assert.deepEqual([day['2026-02-18'].t, day['2026-02-18'].c, day['2026-02-18'].s], ['Ash Wednesday', 'PURPLE', 'LENT']);
assert.equal(day['2026-12-13'].c, 'ROSE');
assert.ok([day['2026-02-07'].t, ...(day['2026-02-07'].o ?? [])].some((t) => /Gonsalo Garcia/.test(t)), 'Gonsalo Garcia Feb 7');
const saints = load('saints.json');
assert.ok(saints.some((s) => s.n === 'Joseph'), 'Joseph');
assert.ok(saints.some((s) => s.n === 'Francis Xavier' && s.m === 12 && s.day === 3), 'Francis Xavier Dec 3');
assert.ok(saints.some((s) => s.n === 'Thomas'), 'Thomas');
console.log('all checks passed');
