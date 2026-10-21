// Same shape as the backend DTO records.

export type AccessReason = 'GRANTED' | 'UNDERAGE' | 'BLOCKED';

export interface Player {
  id: number;
  firstName: string;
  lastName: string;
  /** ISO date, e.g. "1990-05-01" */
  birthDate: string;
  blocked: boolean;
}

export interface CreatePlayerRequest {
  firstName: string;
  lastName: string;
  birthDate: string;
}

export interface AccessCheckResult {
  granted: boolean;
  reason: AccessReason;
  playerName: string;
  /** ISO instant (UTC), e.g. "2026-10-22T10:00:00Z" */
  attemptedAt: string;
}

export interface AccessAttempt {
  id: number;
  playerId: number;
  playerName: string;
  attemptedAt: string;
  granted: boolean;
  reason: AccessReason;
}

/** Spring ProblemDetail (RFC 9457) plus our "errors" map for validation errors. */
export interface ProblemDetail {
  title?: string;
  status?: number;
  detail?: string;
  errors?: Record<string, string>;
}
