package com.veltix.praktikum03

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: MahasiswaAdapter
    private val dataMahasiswa = mutableListOf<Mahasiswa>()

    private lateinit var etNim: EditText
    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etJurusan: EditText
    private lateinit var etNomorHp: EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnBatal: Button

    // null = mode tambah; berisi id = mode ubah.
    private var editId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etNim = findViewById(R.id.etNim)
        etNama = findViewById(R.id.etNama)
        etAlamat = findViewById(R.id.etAlamat)
        etJurusan = findViewById(R.id.etJurusan)
        etNomorHp = findViewById(R.id.etNomorHp)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBatal = findViewById(R.id.btnBatal)
        val listView = findViewById<ListView>(R.id.listMahasiswa)

        db = DatabaseHelper(this)
        adapter = MahasiswaAdapter(this, dataMahasiswa)
        listView.adapter = adapter
        loadData()

        btnSimpan.setOnClickListener { simpan() }
        btnBatal.setOnClickListener { resetForm() }

        // Ketuk baris: isi form dengan data baris tersebut (mode ubah).
        listView.setOnItemClickListener { _, _, position, _ ->
            isiForm(dataMahasiswa[position])
        }

        // Tekan lama: tampilkan konfirmasi hapus.
        listView.setOnItemLongClickListener { _, _, position, _ ->
            konfirmasiHapus(dataMahasiswa[position])
            true
        }
    }

    private fun loadData() {
        dataMahasiswa.clear()
        dataMahasiswa.addAll(db.getAllMahasiswa())
        adapter.notifyDataSetChanged()
    }

    private fun simpan() {
        val nim = etNim.text.toString().trim()
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val jurusan = etJurusan.text.toString().trim()
        val nomorHp = etNomorHp.text.toString().trim()

        if (!validasi(nim, nama, nomorHp)) return

        val id = editId
        val mhs = Mahasiswa(
            id = id ?: 0, nim = nim, nama = nama,
            alamat = alamat, jurusan = jurusan, nomorHp = nomorHp
        )

        val berhasil = if (id == null) {
            db.insertMahasiswa(mhs) != -1L
        } else {
            db.updateMahasiswa(mhs) > 0
        }

        if (berhasil) {
            val aksi = if (id == null) "ditambahkan" else "diperbarui"
            Toast.makeText(this, "Data berhasil $aksi", Toast.LENGTH_SHORT).show()
            resetForm()
            loadData()
        } else {
            Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validasi(nim: String, nama: String, nomorHp: String): Boolean {
        var valid = true

        if (nim.isEmpty()) {
            etNim.error = "NIM wajib diisi"
            valid = false
        } else if (nim.length < 5 || !nim.all { it.isDigit() }) {
            etNim.error = "NIM minimal 5 digit angka"
            valid = false
        } else if (db.isNimTerdaftar(nim, editId ?: 0)) {
            etNim.error = "NIM sudah terdaftar"
            valid = false
        }

        if (nama.isEmpty()) {
            etNama.error = "Nama wajib diisi"
            valid = false
        }

        // Nomor HP boleh kosong; bila diisi harus 10-13 digit dan diawali 08.
        if (nomorHp.isNotEmpty() && !hpValid(nomorHp)) {
            etNomorHp.error = "Nomor HP tidak valid (contoh 081234567890)"
            valid = false
        }

        return valid
    }

    private fun hpValid(hp: String): Boolean {
        return hp.startsWith("08") && hp.length in 10..13 && hp.all { it.isDigit() }
    }

    private fun isiForm(mhs: Mahasiswa) {
        etNim.setText(mhs.nim)
        etNama.setText(mhs.nama)
        etAlamat.setText(mhs.alamat)
        etJurusan.setText(mhs.jurusan)
        etNomorHp.setText(mhs.nomorHp)
        editId = mhs.id
        btnSimpan.text = "Perbarui"
        btnBatal.visibility = android.view.View.VISIBLE
    }

    private fun resetForm() {
        for (et in listOf(etNim, etNama, etAlamat, etJurusan, etNomorHp)) {
            et.text.clear()
            et.error = null
        }
        editId = null
        btnSimpan.text = "Simpan"
        btnBatal.visibility = android.view.View.GONE
    }

    private fun konfirmasiHapus(mhs: Mahasiswa) {
        AlertDialog.Builder(this)
            .setTitle("Hapus data")
            .setMessage("Hapus ${mhs.nama} (${mhs.nim})?")
            .setPositiveButton("Hapus") { _, _ ->
                if (db.deleteMahasiswa(mhs.id) > 0) {
                    Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                    if (editId == mhs.id) resetForm()
                    loadData()
                } else {
                    Toast.makeText(this, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}