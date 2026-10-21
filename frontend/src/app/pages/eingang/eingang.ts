import { httpResource } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { formatBerlinTime, reasonText } from '../../core/format';
import { AccessCheckResult, Player } from '../../core/models';

@Component({
  selector: 'app-eingang',
  imports: [FormsModule],
  templateUrl: './eingang.html',
  styleUrl: './eingang.scss',
})
export class Eingang {
  private readonly api = inject(ApiService);

  /** Signal-based GET: value(), isLoading() and error() are signals. */
  protected readonly players = httpResource<Player[]>(() => '/api/players', { defaultValue: [] });

  protected readonly selectedPlayerId = signal<number | null>(null);
  protected readonly checking = signal(false);
  protected readonly result = signal<AccessCheckResult | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly reasonText = reasonText;
  protected readonly formatBerlinTime = formatBerlinTime;

  protected check(): void {
    const playerId = this.selectedPlayerId();
    if (playerId === null) {
      return;
    }
    this.checking.set(true);
    this.result.set(null);
    this.error.set(null);

    this.api.checkAccess(playerId).subscribe({
      next: (result) => {
        this.result.set(result);
        this.checking.set(false);
      },
      error: () => {
        this.error.set('Die Prüfung ist fehlgeschlagen. Bitte erneut versuchen.');
        this.checking.set(false);
      },
    });
  }
}
