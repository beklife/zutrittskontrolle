import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';

// axe checks rules of WCAG 2.0/2.1 level A + AA (e.g. contrast, labels, ARIA).
const WCAG_TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'];

for (const path of ['/eingang', '/verwaltung', '/protokoll', '/ueber']) {
  test(`keine WCAG-Verstöße auf ${path}`, async ({ page }) => {
    await page.goto(path);
    await expect(page.locator('h1')).toBeVisible();

    const results = await new AxeBuilder({ page }).withTags(WCAG_TAGS).analyze();

    expect(results.violations).toEqual([]);
  });
}

test('keine WCAG-Verstöße mit angezeigtem Ergebnis und Fehlermeldungen', async ({ page }) => {
  await page.goto('/eingang');
  await page.getByLabel('Spieler', { exact: true }).selectOption({ label: 'Probe, Lena' });
  await page.getByRole('button', { name: 'Prüfen' }).click();
  await expect(page.getByTestId('result')).toBeVisible();
  expect((await new AxeBuilder({ page }).withTags(WCAG_TAGS).analyze()).violations).toEqual([]);

  await page.goto('/verwaltung');
  await page.getByRole('button', { name: 'Spieler hinzufügen' }).click();
  await expect(page.getByText('Bitte einen Vornamen eingeben.')).toBeVisible();
  expect((await new AxeBuilder({ page }).withTags(WCAG_TAGS).analyze()).violations).toEqual([]);
});
