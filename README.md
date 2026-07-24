# Bank Sampah Digital Desa

Aplikasi **Bank Sampah Digital Desa** adalah sebuah aplikasi Android berbasis modern yang dibangun untuk memudahkan para pengurus desa dalam mengelola pencatatan dan sirkulasi bank sampah secara digital. Aplikasi ini dirancang agar simpel, informatif, dan sangat ramah pengguna (terutama bagi pengguna yang belum terbiasa dengan teknologi yang rumit).

## 🚀 Fitur Utama

- **Autentikasi Aman:** Sistem login khusus untuk petugas pengurus bank sampah (terintegrasi dengan Firebase Authentication).
- **Dashboard Informatif:** Pantau total nasabah, total sampah (dalam Kg), total saldo, dan transaksi terbaru secara *real-time*.
- **Manajemen Nasabah:** Pencatatan dan pengelolaan data nasabah (nama, alamat, no HP, dan saldo aktif).
- **Manajemen Jenis & Harga Sampah:** Mengatur berbagai macam kategori sampah beserta harganya per satuan (misalnya Plastik, Kertas, Logam, dsb).
- **Transaksi Setor Sampah:** Petugas dapat mencatat sampah yang disetorkan nasabah, lengkap dengan subtotal per jenis, dan sistem akan mengonversinya menjadi penambahan saldo secara otomatis.
- **Transaksi Tarik Saldo:** Fasilitas untuk menarik saldo tunai yang sudah dikumpulkan oleh nasabah.
- **Riwayat Transaksi:** Lacak dan filter seluruh riwayat transaksi (baik setor maupun tarik).
- **Laporan Bulanan:** Rekap data bulanan (Total Setoran, Penarikan, Berat Sampah) dan *Leaderboard* 5 Top Nasabah.

## 🛠️ Tech Stack & Arsitektur

Aplikasi ini dibangun secara native dengan menggunakan _best practices_ terkini untuk pengembangan Android:

- **Bahasa:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) dengan **Material Design 3 (M3)**
- **Arsitektur:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Navigasi:** Jetpack Navigation Compose
- **State Management:** Coroutines & StateFlow / SharedFlow
- **Backend as a Service (BaaS):**
  - **Firebase Authentication:** Untuk sistem Login petugas.
  - **Cloud Firestore:** Database NoSQL *real-time* untuk menyimpan data Pengguna, Nasabah, Jenis Sampah, dan Transaksi.
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) dengan Version Catalogs (`libs.versions.toml`)
- **Minimum SDK:** 28 (Android 9.0 Pie)
- **Target SDK:** 34

## 📦 Panduan Instalasi dan Setup

1. **Clone Repositori:**
   Buka terminal atau Android Studio, lalu *clone* *project* ini.
   ```bash
   git clone <URL_REPOSITORY_ANDA>
   ```

2. **Setup Firebase:**
   - Buka [Firebase Console](https://console.firebase.google.com/).
   - Buat *project* baru bernama "Bank Sampah".
   - Daftarkan aplikasi Android Anda (pastikan `package name` sesuai dengan `com.kkn.banksampah`).
   - Unduh file `google-services.json` dan letakkan di dalam direktori `app/`.
   - Aktifkan fitur **Authentication (Email/Password)** dan **Cloud Firestore**.
   - Tambahkan *Firestore Security Rules* (atur sementara ke mode `test` selama tahap *development*).

3. **Build dan Jalankan Aplikasi:**
   - Buka Android Studio.
   - Tunggu hingga proses *Gradle Sync* selesai.
   - Klik **Run 'app'** (`Shift+F10`) untuk menjalankan aplikasi di Emulator atau perangkat fisik Android Anda.

## 🗂️ Struktur Direktori Proyek

```
app/src/main/java/com/kkn/banksampah/
│
├── data/
│   ├── model/         # Data class (User, Nasabah, Transaksi, dll)
│   └── repository/    # Pengelolaan komunikasi dengan Cloud Firestore & Auth
│
├── navigation/        # Pengaturan rute aplikasi dan AppNavigation.kt
│
├── ui/
│   ├── auth/          # Layar Login
│   ├── components/    # Komponen Reusable (AppTopBar, StatCard, TransactionCard, dll)
│   ├── dashboard/     # Halaman Beranda (Statistik)
│   ├── nasabah/       # Layar Daftar dan Tambah Nasabah
│   ├── sampah/        # Layar Harga dan Jenis Sampah
│   ├── transaksi/     # Layar Setor Sampah dan Tarik Saldo
│   ├── riwayat/       # Layar Riwayat Transaksi
│   ├── laporan/       # Layar Laporan Bulanan
│   ├── pengaturan/    # Layar Info Akun dan Logout
│   └── theme/         # Konfigurasi Tema Compose (Color, Type, Shape)
│
└── util/              # Helper class (Format Mata Uang, Waktu, UiState)
```

## 🔐 Contoh Data Login (Testing)
*(Hanya untuk environment development)*

Karena aplikasi tidak menyediakan pendaftaran *(Sign Up)* dari dalam UI aplikasi (hanya admin/petugas resmi), Anda harus menambahkan user pertama kali melalui [Firebase Authentication Console](https://console.firebase.google.com/):
- **Email:** `petugas@desa.com`
- **Password:** `123456`

## 🤝 Kontribusi & Pengembangan Lanjutan
Aplikasi ini diinisiasi untuk proyek KKN (Kuliah Kerja Nyata) dengan fleksibilitas yang sangat tinggi untuk dikembangkan lebih lanjut. Ke depannya, aplikasi dapat diperluas dengan fitur seperti:
- Export data transaksi ke format PDF atau Excel.
- Level akses pengguna (Admin vs Petugas biasa).
- Fitur notifikasi jika ada transaksi baru.

---
Dibuat dengan ❤️ untuk kemajuan Digitalisasi Desa.
