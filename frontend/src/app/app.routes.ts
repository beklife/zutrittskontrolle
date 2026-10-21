import { Routes } from '@angular/router';

// Lazy loading: each page is its own JS chunk. "title" sets the browser tab title (also read by screen readers).
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'eingang' },
  {
    path: 'eingang',
    title: 'Eingang – Zutrittskontrolle',
    loadComponent: () => import('./pages/eingang/eingang').then((m) => m.Eingang),
  },
  {
    path: 'verwaltung',
    title: 'Verwaltung – Zutrittskontrolle',
    loadComponent: () => import('./pages/verwaltung/verwaltung').then((m) => m.Verwaltung),
  },
  {
    path: 'protokoll',
    title: 'Protokoll – Zutrittskontrolle',
    loadComponent: () => import('./pages/protokoll/protokoll').then((m) => m.Protokoll),
  },
  {
    path: 'ueber',
    title: 'Über das Projekt – Zutrittskontrolle',
    loadComponent: () => import('./pages/ueber/ueber').then((m) => m.Ueber),
  },
  { path: '**', redirectTo: 'eingang' },
];
