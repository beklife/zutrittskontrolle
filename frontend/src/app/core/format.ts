import { AccessReason } from './models';

const BUSINESS_ZONE = 'Europe/Berlin';

/** "1990-05-01" -> "01.05.1990" (no Date object, so no time zone shift). */
export function formatIsoDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-');
  return `${day}.${month}.${year}`;
}

/** UTC instant -> "14:05:09" in Berlin time. */
export function formatBerlinTime(instant: string): string {
  return new Intl.DateTimeFormat('de-DE', {
    timeZone: BUSINESS_ZONE,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).format(new Date(instant));
}

/** Today in Berlin as "YYYY-MM-DD" (the format of <input type="date"> and of the API). */
export function todayInBerlin(): string {
  return new Intl.DateTimeFormat('en-CA', { timeZone: BUSINESS_ZONE }).format(new Date());
}

export function reasonText(reason: AccessReason): string {
  switch (reason) {
    case 'GRANTED':
      return 'Zutritt erlaubt';
    case 'UNDERAGE':
      return 'Zutritt verweigert: unter 18';
    case 'BLOCKED':
      return 'Zutritt verweigert: gesperrt';
  }
}
