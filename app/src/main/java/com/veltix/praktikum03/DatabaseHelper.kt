package com.veltix.praktikum03

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_MAHASISWA (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nim TEXT NOT NULL UNIQUE, " +
                "nama TEXT NOT NULL, " +
                "alamat TEXT, " +
                "jurusan TEXT, " +
                "nomor_hp TEXT)"
        db.execSQL(createTable)
        seedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MAHASISWA")
        onCreate(db)
    }

    private fun seedData(db: SQLiteDatabase) {
        val seed = listOf(
            Mahasiswa(nim = "21001", nama = "Ahmad Fauzi",
                alamat = "Jl. Pettarani No. 5", jurusan = "Sistem Informasi",
                nomorHp = "081234567890"),
            Mahasiswa(nim = "21002", nama = "Siti Nurhaliza",
                alamat = "Jl. Perintis Kemerdekaan", jurusan = "Sistem Informasi",
                nomorHp = "082198765432"),
            Mahasiswa(nim = "21003", nama = "Budi Santoso",
                alamat = "Jl. Rappocini No. 12", jurusan = "Sistem Informasi",
                nomorHp = "081345678901")
        )
        seed.forEach { db.insert(TABLE_MAHASISWA, null, toValues(it)) }
    }

    private fun toValues(mhs: Mahasiswa) = ContentValues().apply {
        put("nim", mhs.nim)
        put("nama", mhs.nama)
        put("alamat", mhs.alamat)
        put("jurusan", mhs.jurusan)
        put("nomor_hp", mhs.nomorHp)
    }

    // Create: mengembalikan id baris baru, atau -1 bila gagal.
    fun insertMahasiswa(mhs: Mahasiswa): Long =
        writableDatabase.insert(TABLE_MAHASISWA, null, toValues(mhs))

    // Update: mengembalikan jumlah baris yang berubah (0 = tidak ada yang cocok).
    fun updateMahasiswa(mhs: Mahasiswa): Int =
        writableDatabase.update(
            TABLE_MAHASISWA, toValues(mhs), "id = ?", arrayOf(mhs.id.toString())
        )

    // Delete: mengembalikan jumlah baris yang terhapus.
    fun deleteMahasiswa(id: Int): Int =
        writableDatabase.delete(TABLE_MAHASISWA, "id = ?", arrayOf(id.toString()))

    // True bila NIM sudah dipakai baris lain (exceptId = baris yang sedang diubah).
    fun isNimTerdaftar(nim: String, exceptId: Int = 0): Boolean {
        val cursor = readableDatabase.query(
            TABLE_MAHASISWA, arrayOf("id"), "nim = ? AND id != ?",
            arrayOf(nim, exceptId.toString()), null, null, null
        )
        return cursor.use { it.moveToFirst() }
    }

    fun getAllMahasiswa(): List<Mahasiswa> {
        val list = mutableListOf<Mahasiswa>()
        val cursor = readableDatabase.query(
            TABLE_MAHASISWA, null, null, null, null, null, "nama ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Mahasiswa(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        nim = it.getString(it.getColumnIndexOrThrow("nim")),
                        nama = it.getString(it.getColumnIndexOrThrow("nama")),
                        alamat = it.getString(it.getColumnIndexOrThrow("alamat")),
                        jurusan = it.getString(it.getColumnIndexOrThrow("jurusan")),
                        nomorHp = it.getString(it.getColumnIndexOrThrow("nomor_hp"))
                    )
                )
            }
        }
        return list
    }

    companion object {
        private const val DB_NAME = "praktikum.db"
        private const val DB_VERSION = 1
        const val TABLE_MAHASISWA = "mahasiswa"
    }
}