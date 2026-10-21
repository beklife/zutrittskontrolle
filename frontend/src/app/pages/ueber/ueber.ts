import { Component } from '@angular/core';

const GITHUB_URL = 'https://github.com/beklife/zutrittskontrolle';

@Component({
  selector: 'app-ueber',
  templateUrl: './ueber.html',
  styleUrl: './ueber.scss',
})
export class Ueber {
  protected readonly githubUrl = GITHUB_URL;
}
