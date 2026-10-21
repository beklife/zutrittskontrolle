import { expect, test } from '@playwright/test';

// Uses the seeded demo players: Erika Mustermann (adult), Lena Probe (16), Tom Testmann (blocked).

test.describe('Eingang', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/eingang');
  });

  test('erwachsener Spieler darf eintreten', async ({ page }) => {
    await page.getByLabel('Spieler', { exact: true }).selectOption({ label: 'Mustermann, Erika' });
    await page.getByRole('button', { name: 'Prüfen' }).click();

    await expect(page.getByTestId('result')).toContainText('Zutritt erlaubt');
    await expect(page.getByTestId('result')).toContainText('Erika Mustermann');
  });

  test('minderjähriger Spieler wird abgewiesen', async ({ page }) => {
    await page.getByLabel('Spieler', { exact: true }).selectOption({ label: 'Probe, Lena' });
    await page.getByRole('button', { name: 'Prüfen' }).click();

    await expect(page.getByTestId('result')).toContainText('Zutritt verweigert: unter 18');
  });

  test('gesperrter Spieler wird abgewiesen', async ({ page }) => {
    await page.getByLabel('Spieler', { exact: true }).selectOption({ label: 'Testmann, Tom' });
    await page.getByRole('button', { name: 'Prüfen' }).click();

    await expect(page.getByTestId('result')).toContainText('Zutritt verweigert: gesperrt');
  });

  test('Prüfung geht nur mit Tastatur', async ({ page }) => {
    await page.getByLabel('Spieler', { exact: true }).focus();
    await page.getByLabel('Spieler', { exact: true }).selectOption({ label: 'Mustermann, Erika' });
    await page.keyboard.press('Tab');
    await expect(page.getByRole('button', { name: 'Prüfen' })).toBeFocused();
    await page.keyboard.press('Enter');

    await expect(page.getByTestId('result')).toContainText('Zutritt erlaubt');
  });
});

test.describe('Verwaltung', () => {
  test('zeigt Fehlermeldungen bei leerem Formular', async ({ page }) => {
    await page.goto('/verwaltung');
    await page.getByRole('button', { name: 'Spieler hinzufügen' }).click();

    await expect(page.getByText('Bitte einen Vornamen eingeben.')).toBeVisible();
    await expect(page.getByText('Bitte einen Nachnamen eingeben.')).toBeVisible();
    await expect(page.getByText('Bitte ein Geburtsdatum eingeben.')).toBeVisible();
    await expect(page.getByLabel('Vorname')).toBeFocused();
  });

  test('Spieler anlegen, sperren, prüfen und im Protokoll sehen', async ({ page }) => {
    const lastName = `E2e${Date.now()}`;

    await page.goto('/verwaltung');
    await page.getByLabel('Vorname').fill('Paula');
    await page.getByLabel('Nachname').fill(lastName);
    await page.getByLabel('Geburtsdatum').fill('1990-01-15');
    await page.getByRole('button', { name: 'Spieler hinzufügen' }).click();

    await expect(page.getByRole('status')).toHaveText(`Paula ${lastName} wurde hinzugefügt.`);
    const row = page.getByRole('row', { name: new RegExp(lastName) });
    await expect(row).toContainText('Aktiv');

    await page.getByRole('button', { name: `Paula ${lastName} sperren` }).click();
    await expect(row).toContainText('Gesperrt');

    await page.getByRole('link', { name: 'Eingang' }).click();
    await page.getByLabel('Spieler', { exact: true }).selectOption({ label: `${lastName}, Paula` });
    await page.getByRole('button', { name: 'Prüfen' }).click();
    await expect(page.getByTestId('result')).toContainText('Zutritt verweigert: gesperrt');

    await page.getByRole('link', { name: 'Protokoll' }).click();
    await expect(page.getByRole('row', { name: new RegExp(`Paula ${lastName}`) })).toContainText(
      'Zutritt verweigert: gesperrt',
    );
  });
});

test('Demo-Banner ist auf jeder Seite sichtbar', async ({ page }) => {
  for (const path of ['/eingang', '/verwaltung', '/protokoll', '/ueber']) {
    await page.goto(path);
    await expect(page.getByText('Demo mit erfundenen Daten. Wird täglich um 3 Uhr zurückgesetzt.')).toBeVisible();
  }
});
