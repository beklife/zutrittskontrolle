import { HttpErrorResponse, httpResource } from '@angular/common/http';
import { Component, ElementRef, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { formatIsoDate, todayInBerlin } from '../../core/format';
import { Player, ProblemDetail } from '../../core/models';

/** Same rules as the backend: not only spaces, date in the past. */
function notBlank(control: AbstractControl): ValidationErrors | null {
  return typeof control.value === 'string' && control.value.trim().length === 0 ? { required: true } : null;
}

function pastDate(control: AbstractControl): ValidationErrors | null {
  return control.value && control.value >= todayInBerlin() ? { pastDate: true } : null;
}

@Component({
  selector: 'app-verwaltung',
  imports: [ReactiveFormsModule],
  templateUrl: './verwaltung.html',
  styleUrl: './verwaltung.scss',
})
export class Verwaltung {
  private readonly api = inject(ApiService);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly players = httpResource<Player[]>(() => '/api/players', { defaultValue: [] });
  protected readonly status = signal('');
  protected readonly serverError = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly maxDate = todayInBerlin();
  protected readonly formatIsoDate = formatIsoDate;

  protected readonly form = inject(FormBuilder).nonNullable.group({
    firstName: ['', [Validators.required, notBlank, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, notBlank, Validators.maxLength(50)]],
    birthDate: ['', [Validators.required, pastDate]],
  });

  protected toggleBlocked(player: Player): void {
    this.api.setBlocked(player.id, !player.blocked).subscribe({
      next: (updated) => {
        this.players.reload();
        this.status.set(`${updated.firstName} ${updated.lastName} ist jetzt ${updated.blocked ? 'gesperrt' : 'entsperrt'}.`);
      },
      error: () => this.status.set('Die Änderung ist fehlgeschlagen.'),
    });
  }

  protected showError(field: 'firstName' | 'lastName' | 'birthDate'): boolean {
    const control = this.form.controls[field];
    return control.invalid && control.touched;
  }

  /** Keyboard and screen reader users land directly on the first wrong field. Input ids = control names. */
  private focusFirstInvalidField(): void {
    const firstInvalid = Object.entries(this.form.controls).find(([, control]) => control.invalid)?.[0];
    if (firstInvalid) {
      this.host.nativeElement.querySelector<HTMLElement>(`#${firstInvalid}`)?.focus();
    }
  }

  protected submit(): void {
    this.serverError.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.focusFirstInvalidField();
      return;
    }
    this.saving.set(true);
    this.api.createPlayer(this.form.getRawValue()).subscribe({
      next: (player) => {
        this.saving.set(false);
        this.form.reset();
        this.players.reload();
        this.status.set(`${player.firstName} ${player.lastName} wurde hinzugefügt.`);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        const problem = error.error as ProblemDetail | null;
        const messages = problem?.errors ? Object.values(problem.errors) : [problem?.detail];
        this.serverError.set(messages.filter(Boolean).join(' ') || 'Speichern ist fehlgeschlagen.');
      },
    });
  }
}
