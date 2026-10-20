# AdCloser

Usługa ułatwień dostępu, która w wybranych aplikacjach wykrywa przycisk zamknięcia reklamy (X / Zamknij / Close / Skip) i automatycznie go klika.

## Budowanie APK

Opcja A, Android Studio (komputer): File → Open → wybierz folder AdCloser, poczekaj na synchronizację, potem Build → Build App Bundle(s) / APK(s) → Build APK(s). Plik: app/build/outputs/apk/debug/app-debug.apk

Opcja B, GitHub (bez instalowania niczego): utwórz nowe repozytorium, wgraj całą zawartość folderu (razem z ukrytym folderem .github). W zakładce Actions uruchomi się „Build APK”. Po kilku minutach pobierzesz APK z sekcji Artifacts.

## Pierwsze uruchomienie

1. Zainstaluj APK (zezwól na instalację z nieznanych źródeł).
2. Android 13+: Ustawienia → Aplikacje → AdCloser → ⋮ (prawy górny róg) → „Zezwól na ustawienia z ograniczeniami”. Bez tego nie da się włączyć usługi.
3. W aplikacji zaznacz aplikację z reklamami i kliknij „Włącz usługę”, potem włącz AdCloser na liście ułatwień dostępu.
4. Sekcja „Ostatnie akcje” pokazuje, co zostało kliknięte.
