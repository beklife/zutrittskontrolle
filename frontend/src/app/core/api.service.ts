import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AccessCheckResult, CreatePlayerRequest, Player } from './models';

/** Write operations. Read operations use httpResource directly in the pages. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  createPlayer(request: CreatePlayerRequest): Observable<Player> {
    return this.http.post<Player>('/api/players', request);
  }

  setBlocked(playerId: number, blocked: boolean): Observable<Player> {
    return this.http.put<Player>(`/api/players/${playerId}/block`, { blocked });
  }

  checkAccess(playerId: number): Observable<AccessCheckResult> {
    return this.http.post<AccessCheckResult>('/api/access-checks', { playerId });
  }
}
