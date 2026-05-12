# 📺 Digital Signage Client (Android)

**Digital Signage Client** adalah aplikasi pemutar media berbasis Android yang dirancang khusus untuk layar periklanan, papan informasi digital (kiosk), dan monitor komersial. Aplikasi ini beroperasi secara mandiri di latar depan untuk menampilkan antrean gambar dan video secara *looping* berdasarkan jadwal dan *playlist* yang dikonfigurasi melalui server terpusat.

Menggunakan pendekatan **Offline-First**, aplikasi mengunduh seluruh aset media ke penyimpanan lokal perangkat sehingga pemutaran konten tetap berjalan lancar tanpa jeda (*seamless*) meskipun terjadi gangguan koneksi internet.

---

## ✨ Fitur Utama

- **🔄 Sinkronisasi Otomatis Berbasis ID**: Menggunakan `ANDROID_ID` perangkat sebagai identitas unik (Serial Number) untuk mengambil konfigurasi, jadwal, dan *playlist* dari server backend secara berkala.
- **📅 Penjadwalan Cerdas (*Smart Scheduling*)**: Mendukung penentuan *playlist* aktif berdasarkan rentang tanggal, hari dalam seminggu, jam tayang, serta tingkat prioritas tayangan.
- **💾 Offline-First & Cache Lokal**: Mengunduh berkas aset (video/gambar) secara asinkron di latar belakang menggunakan mekanisme *hash MD5* untuk validasi integritas data.
- **🎞️ Pemutaran Media Tanpa Jeda**: Menjalankan transisi media berdasarkan urutan (*order*) dan durasi tayang (*duration*) yang diatur pada CMS menggunakan pemutar video teroptimasi.
- **📱 Mode Layar Penuh (*Kiosk / Immersive*)**: Antarmuka dirancang untuk terus aktif di layar penuh tanpa gangguan *status bar* atau navigasi.

---

## 🛠️ Teknologi & Arsitektur

Aplikasi ini dibangun menggunakan bahasa **Kotlin** dengan mengadopsi prinsip arsitektur modern Android (*Clean Architecture* & MVVM):

- **Antarmuka Pengguna (UI)**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material 3
- **Pemutar Media**: [AndroidX Media3 ExoPlayer](https://developer.android.com/media/media3) (Video) & [Coil](https://coil-kt.github.io/coil/) (Gambar)
- **Injeksi Dependensi (DI)**: [Dagger Hilt](https://dagger.dev/hilt/)
- **Basis Data Lokal**: [Room Database](https://developer.android.com/training/data-storage/room)
- **Pekerjaan Latar Belakang**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) (`SyncWorker` & `DownloadWorker`)
- **Jaringan & API**: [Retrofit2](https://square.github.io/retrofit/) & OkHttp3

---

## 🚀 Memulai (*Getting Started*)

### Persyaratan Sistem

- **Minimum SDK**: Android 7.0 (API Level 24)
- **Target SDK**: Android 16 (API Level 36)
- **Android Studio**: Versi terbaru yang mendukung integrasi Jetpack Compose dan KSP.

### Instalasi & Konfigurasi

#### 1. Kloning Repositori

```bash
git clone https://github.com/username/digital-signage-client.git
```

#### 2. Konfigurasi URL Server Backend

Buka berkas `AppModule.kt` yang terletak di direktori:

```plaintext
app/src/main/java/com/ppwb/digitalsignage/di/
```

Lalu sesuaikan alamat `baseUrl` dengan server staging atau produksi Anda:

```kotlin
@Provides
@Singleton
fun provideSignageApi(): SignageApi {
    return Retrofit.Builder()
        .baseUrl("http://digisign.test/") // Ganti dengan URL Backend Anda
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SignageApi::class.java)
}
```

#### 3. Bangun dan Jalankan

1. Buka proyek di Android Studio.
2. Tunggu proses sinkronisasi Gradle selesai.
3. Jalankan pada perangkat fisik monitor/STB Android untuk mendapatkan pembacaan `ANDROID_ID` yang persisten.

---

## 📡 Spesifikasi API (Endpoint Sinkronisasi)

Aplikasi melakukan request berkala ke endpoint berikut untuk mendapatkan status terbaru perangkat:

```http
GET /api/devices/{serial_number}/sync?current_version={version}
```

### Respons JSON yang Diharapkan

```json
{
  "version": "v1.0.2",
  "device": {
    "id": "dev_01",
    "device_name": "Lobby Display",
    "serial_number": "a1b2c3d4e5f6",
    "location": "Main Lobby",
    "default_playlist_id": "plist_default"
  },
  "schedules": [
    {
      "id": "sch_01",
      "playlist_id": "plist_morning",
      "priority": 1,
      "valid_from_date": "2026-01-01",
      "valid_until_date": "2026-12-31",
      "time_start": "08:00:00",
      "time_end": "12:00:00",
      "days_of_week": [2, 3, 4, 5, 6]
    }
  ],
  "playlists": [
    {
      "id": "plist_morning",
      "name": "Morning Promo",
      "items": [
        {
          "asset_id": "asset_video_01",
          "order": 1,
          "duration_sec": 30
        }
      ]
    }
  ],
  "assets": [
    {
      "id": "asset_video_01",
      "name": "Promo_BCA.mp4",
      "type": "video",
      "file_url": "http://digisign.test/storage/assets/video_01.mp4",
      "hash_md5": "e99a18c428cb38d5f260853678922e03",
      "size_bytes": 15482012
    }
  ]
}
```

> **Catatan:** Jika tidak ada perubahan versi data di server, API dapat mengembalikan kode HTTP `304 Not Modified` untuk menghemat bandwidth.

---

## 📂 Struktur Folder Proyek

```plaintext
app/src/main/java/com/ppwb/digitalsignage/
│
├── data/             # Lapisan Data (Lokal Room DB, Remote Retrofit API, Repositori)
├── di/               # Konfigurasi Dependensi menggunakan Dagger Hilt
├── domain/           # Model Objek Data (Device, Schedule, Playlist, Asset)
├── presentation/     # Antarmuka Pengguna Compose (Screen & ViewModel)
└── worker/           # Layanan Latar Belakang (Sinkronisasi API & Pengunduhan Aset)
```
