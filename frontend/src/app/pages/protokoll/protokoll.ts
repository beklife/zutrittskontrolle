import { httpResource } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { formatBerlinTime, formatIsoDate, reasonText, todayInBerlin } from '../../core/format';
import { AccessAttempt } from '../../core/models';

@Component({
  selector: 'app-protokoll',
  templateUrl: './protokoll.html',
})
export class Protokoll {
  protected readonly date = signal(todayInBerlin());

  /** Reloads automatically whenever the date signal changes. */
  protected readonly attempts = httpResource<AccessAttempt[]>(
    () => ({ url: '/api/access-attempts', params: { date: this.date() } }),
    { defaultValue: [] },
  );

  protected readonly formatBerlinTime = formatBerlinTime;
  protected readonly formatIsoDate = formatIsoDate;
  protected readonly reasonText = reasonText;

  protected onDateChange(value: string): void {
    if (value) {
      this.date.set(value);
    }
  }
}
