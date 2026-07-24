package com.kkn.banksampah.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import com.kkn.banksampah.data.repository.LaporanBulanan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PdfHelper {
    
    suspend fun generatePdf(context: Context, uri: Uri, laporan: LaporanBulanan, bulan: String, tahun: String) {
        withContext(Dispatchers.IO) {
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                var currentPageNumber = 1
                var page = pdfDocument.startPage(pageInfo)
                var canvas: Canvas = page.canvas
                val paint = Paint()

                // Header
                paint.color = Color.BLACK
                paint.textSize = 22f
                paint.isFakeBoldText = true
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Bank Sampah Digital Desa", pageInfo.pageWidth / 2f, 60f, paint)

                paint.textSize = 16f
                paint.isFakeBoldText = false
                canvas.drawText("Laporan Bulanan: $bulan $tahun", pageInfo.pageWidth / 2f, 85f, paint)

                paint.strokeWidth = 2f
                canvas.drawLine(40f, 100f, pageInfo.pageWidth - 40f, 100f, paint)

                // Summary Section
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 13f
                paint.isFakeBoldText = true
                canvas.drawText("Ringkasan Performa", 40f, 130f, paint)
                
                paint.textSize = 11f
                paint.isFakeBoldText = false
                canvas.drawText("Total Setoran: ${CurrencyHelper.formatRupiah(laporan.totalSetor)}", 40f, 150f, paint)
                canvas.drawText("Total Penarikan: ${CurrencyHelper.formatRupiah(laporan.totalTarik)}", 40f, 168f, paint)
                canvas.drawText("Total Berat Sampah: ${laporan.totalKg} Kg", 40f, 186f, paint)
                canvas.drawText("Total Transaksi: ${laporan.jumlahTransaksi}", 40f, 204f, paint)

                // Bar Chart Section
                paint.textSize = 13f
                paint.isFakeBoldText = true
                canvas.drawText("Grafik Setoran vs Penarikan", 40f, 240f, paint)
                paint.isFakeBoldText = false

                val maxVal = maxOf(laporan.totalSetor, laporan.totalTarik)
                val chartHeight = 100f
                val chartBaseY = 370f

                if (maxVal > 0) {
                    val scale = chartHeight / maxVal.toFloat()
                    
                    // Setoran Bar (Green)
                    val setorHeight = (laporan.totalSetor.toFloat() * scale)
                    paint.color = Color.parseColor("#16A34A")
                    canvas.drawRect(80f, chartBaseY - setorHeight, 150f, chartBaseY, paint)
                    paint.color = Color.BLACK
                    paint.textSize = 10f
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("Setoran", 115f, chartBaseY + 15f, paint)
                    canvas.drawText(CurrencyHelper.formatRupiah(laporan.totalSetor), 115f, chartBaseY - setorHeight - 6f, paint)
                    
                    // Penarikan Bar (Red)
                    val tarikHeight = (laporan.totalTarik.toFloat() * scale)
                    paint.color = Color.parseColor("#DC2626")
                    canvas.drawRect(220f, chartBaseY - tarikHeight, 290f, chartBaseY, paint)
                    paint.color = Color.BLACK
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("Penarikan", 255f, chartBaseY + 15f, paint)
                    canvas.drawText(CurrencyHelper.formatRupiah(laporan.totalTarik), 255f, chartBaseY - tarikHeight - 6f, paint)
                } else {
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText("Belum ada data untuk digambar.", 40f, 270f, paint)
                }

                // Table Section
                paint.color = Color.BLACK
                paint.textAlign = Paint.Align.LEFT
                paint.textSize = 13f
                paint.isFakeBoldText = true
                canvas.drawText("Tabel Daftar Penyetor Sampah", 40f, 420f, paint)
                
                // Table Headers
                var yPos = 445f
                paint.textSize = 11f
                paint.isFakeBoldText = true
                canvas.drawText("No", 40f, yPos, paint)
                canvas.drawText("Nama Penyetor", 80f, yPos, paint)
                canvas.drawText("Berat (Kg)", 320f, yPos, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("Total Setor (Rp)", pageInfo.pageWidth - 40f, yPos, paint)
                
                paint.strokeWidth = 1f
                canvas.drawLine(40f, yPos + 6f, pageInfo.pageWidth - 40f, yPos + 6f, paint)
                
                yPos += 24f
                paint.isFakeBoldText = false

                val listPenyetor = laporan.daftarPenyetor
                if (listPenyetor.isEmpty()) {
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText("Belum ada data penyetor pada bulan ini.", 40f, yPos, paint)
                } else {
                    listPenyetor.forEachIndexed { index, item ->
                        if (yPos > pageInfo.pageHeight - 60) {
                            pdfDocument.finishPage(page)
                            currentPageNumber++
                            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNumber).create()
                            page = pdfDocument.startPage(newPageInfo)
                            canvas = page.canvas
                            yPos = 50f
                        }

                        paint.textAlign = Paint.Align.LEFT
                        canvas.drawText("${index + 1}", 40f, yPos, paint)
                        canvas.drawText(item.nama, 80f, yPos, paint)
                        canvas.drawText("${item.totalKg} Kg", 320f, yPos, paint)
                        
                        paint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(CurrencyHelper.formatRupiah(item.totalSetor), pageInfo.pageWidth - 40f, yPos, paint)
                        
                        canvas.drawLine(40f, yPos + 6f, pageInfo.pageWidth - 40f, yPos + 6f, paint)
                        yPos += 22f
                    }
                }

                pdfDocument.finishPage(page)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }

                pdfDocument.close()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Berhasil menyimpan PDF Laporan", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal menyimpan PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
