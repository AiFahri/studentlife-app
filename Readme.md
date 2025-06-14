# Student Life App - Aplikasi Manajemen Aktivitas Mahasiswa

## Deskripsi Aplikasi

Student Life adalah aplikasi yang dirancang untuk membantu mahasiswa dalam mengelola kegiatan akademik dan keuangan mereka secara efisien. Aplikasi ini memiliki empat fitur utama:

- **Tambah Jadwal Kuliah**
- **Catat Tugas Kuliah**
- **Catat Pengeluaran Harian**
- **Rekomendasi Tempat Belajar**

## Fitur Utama

- **Input Data**
  - Tambahkan data seperti jadwal kuliah, tugas, tempat belajar rekomendasi, dan pengeluaran.
  
- **Upload Gambar Pendukung**
  - Lampirkan gambar untuk memperkaya informasi yang diinput.

- **Edit & Hapus Data**
  - Semua data yang dimasukkan dapat diedit dan dihapus kapan saja.

- **Tampilan Dinamis**
  - Data ditampilkan menggunakan `RecyclerView` dan `Custom Adapter` untuk navigasi yang lancar dan terstruktur.

- **Firebase Authentication**
  - Login menggunakan:
    - Email/Password
    - OAuth (Google Sign-In dan provider lainnya)

- **Firebase Realtime Database**
  - Operasi CRUD (Create, Read, Update, Delete) dilakukan secara real-time dengan Firebase Realtime Database.

## Kontribusi Setiap Anggota

**Fahri Nuril Fikri – 235150700111049**

- Membuat 3 Activity & Layout beserta event handling, Intent, dan RecyclerView untuk fitur **Tambah Jadwal Kuliah**.
- mengimplementasikan halaman Login, halaman Menu Utama, serta mengintegrasikan Firebase Authentication untuk login menggunakan email/password dan Google OAuth.
- Menghubungkan fitur Tambah Jadwal Kuliah dengan Firebase Realtime Database untuk menyimpan, membaca, mengedit, dan menghapus data jadwal secara real-time (CRUD).

**Humaam Ahmad Yaasiin – 235150707111049**

- Membuat 3 Activity & Layout beserta event handling, Intent, dan RecyclerView untuk fitur **Catat Tugas Kuliah**.
- Mengintegrasikan fitur Catat Tugas Kuliah dengan Firebase Realtime Database, termasuk fungsionalitas CRUD data tugas.
- Berkontribusi pada pengelolaan akun pengguna dan validasi login melalui Firebase Authentication.
  
**Raynanta Aulia Nanda – 235150701111045**

- Membuat 3 Activity & Layout beserta event handling, Intent, dan RecyclerView untuk fitur **Rekomendasi Tempat Belajar**.
- Mendesain dan Menerapkan fitur penyimpanan dan pengambilan data tempat belajar dari Firebase Realtime Database secara real-time.
- Berperan dalam konfigurasi dan integrasi Google OAuth agar pengguna dapat login menggunakan akun Google mereka.

**M. Daffa Riyadlussalam – 235150700111043**

- Membuat 3 Activity & Layout beserta event handling, Intent, dan RecyclerView untuk fitur **Catat Pengeluaran Harian**.
- Mengimplementasikan fitur CRUD data pengeluaran menggunakan Firebase Realtime Database.
- Membantu debugging dan pengujian fitur autentikasi Firebase untuk memastikan keamanan dan kenyamanan pengguna.

## Cara Menjalankan Aplikasi

1. Clone repositori ini:

   ```bash
   git clone https://github.com/AiFahri/studentlife-app.git
   ```

2. Buka proyek di Android Studio.

3. Pilih perangkat atau emulator untuk menjalankan aplikasi.

4. Klik "Run" untuk memulai aplikasi.
