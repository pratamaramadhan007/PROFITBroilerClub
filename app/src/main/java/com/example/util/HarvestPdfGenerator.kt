package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.HarvestTicket
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object HarvestPdfGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points
    private const val MARGIN = 36f // 0.5 inch margin
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    /**
     * Generate an official PDF document containing styled tables, metadata, and signatures.
     */
    fun generatePdf(context: Context, ticket: HarvestTicket): File {
        val pdfDocument = PdfDocument()

        // Paints
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225) // Slate 300
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val rowsPerPage = 18
        val totalEntries = ticket.entries.size
        val totalPages = ((totalEntries + rowsPerPage - 1) / rowsPerPage).coerceAtLeast(1)

        for (pageIndex in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var currentY = MARGIN

            // 1. Header Banner (Only on first page, or simplified on later pages)
            if (pageIndex == 0) {
                // Header Background Banner
                fillPaint.color = Color.rgb(15, 23, 42) // Cyber Slate Dark
                canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + 68f), 8f, 8f, fillPaint)

                // Accent Top Line (Emerald)
                fillPaint.color = Color.rgb(16, 185, 129) // Emerald 500
                canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + 4f), 2f, 2f, fillPaint)

                // Header Texts
                boldPaint.color = Color.WHITE
                boldPaint.textSize = 15f
                canvas.drawText("PROFIT BROILER CLUB", MARGIN + 16f, currentY + 26f, boldPaint)

                textPaint.color = Color.rgb(52, 211, 153) // Emerald accent
                textPaint.textSize = 10f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("SURAT JALAN & BERITA ACARA TIMBANGAN PANEN AYAM BROILER", MARGIN + 16f, currentY + 42f, textPaint)

                textPaint.color = Color.rgb(148, 163, 184) // Slate 400
                textPaint.textSize = 8.5f
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Closed House Modern 2 Lantai • Sistem Timbangan Digital Terverifikasi", MARGIN + 16f, currentY + 56f, textPaint)

                currentY += 78f

                // 2. Metadata Box (2 Columns)
                val metaBoxHeight = 76f
                fillPaint.color = Color.rgb(248, 250, 252) // Slate 50
                canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + metaBoxHeight), 6f, 6f, fillPaint)
                linePaint.color = Color.rgb(226, 232, 240)
                canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + metaBoxHeight), 6f, 6f, linePaint)

                // Column 1 (Left)
                val col1X = MARGIN + 14f
                val col1ValX = MARGIN + 85f
                var metaY = currentY + 18f

                textPaint.color = Color.rgb(100, 116, 139)
                textPaint.textSize = 9f
                boldPaint.color = Color.rgb(15, 23, 42)
                boldPaint.textSize = 9f

                canvas.drawText("No. Tiket", col1X, metaY, textPaint)
                canvas.drawText(":  ${ticket.ticketNumber}", col1ValX, metaY, boldPaint)

                metaY += 16f
                canvas.drawText("Hari / Tanggal", col1X, metaY, textPaint)
                canvas.drawText(":  ${ticket.dayName}, ${ticket.date}", col1ValX, metaY, boldPaint)

                metaY += 16f
                canvas.drawText("Waktu Panen", col1X, metaY, textPaint)
                canvas.drawText(":  ${ticket.time}", col1ValX, metaY, boldPaint)

                metaY += 16f
                canvas.drawText("Asal Kandang", col1X, metaY, textPaint)
                canvas.drawText(":  ${ticket.floor.displayName}", col1ValX, metaY, boldPaint)

                // Column 2 (Right)
                val col2X = MARGIN + 270f
                val col2ValX = MARGIN + 345f
                metaY = currentY + 18f

                canvas.drawText("Peternak", col2X, metaY, textPaint)
                canvas.drawText(":  ${ticket.farmerName}", col2ValX, metaY, boldPaint)

                metaY += 16f
                canvas.drawText("Pembeli / PT", col2X, metaY, textPaint)
                canvas.drawText(":  ${ticket.buyerName}", col2ValX, metaY, boldPaint)

                metaY += 16f
                canvas.drawText("Nama Driver", col2X, metaY, textPaint)
                canvas.drawText(":  ${ticket.driverName}", col2ValX, metaY, boldPaint)

                metaY += 16f
                canvas.drawText("No Plat Truk", col2X, metaY, textPaint)
                canvas.drawText(":  ${ticket.vehiclePlate}", col2ValX, metaY, boldPaint)

                currentY += metaBoxHeight + 14f
            } else {
                // Continuation Header for subsequent pages
                boldPaint.color = Color.rgb(15, 23, 42)
                boldPaint.textSize = 11f
                canvas.drawText("PROFIT BROILER CLUB - Tiket Panen ${ticket.ticketNumber} (Lanjutan)", MARGIN, currentY + 12f, boldPaint)
                textPaint.color = Color.rgb(100, 116, 139)
                textPaint.textSize = 9f
                canvas.drawText("Driver: ${ticket.driverName} • No Plat: ${ticket.vehiclePlate}", MARGIN + CONTENT_WIDTH - 180f, currentY + 12f, textPaint)
                currentY += 24f
            }

            // 3. Table Header
            val tableY = currentY
            val headerHeight = 22f

            fillPaint.color = Color.rgb(30, 41, 59) // Dark Slate
            canvas.drawRect(MARGIN, tableY, MARGIN + CONTENT_WIDTH, tableY + headerHeight, fillPaint)

            boldPaint.color = Color.WHITE
            boldPaint.textSize = 8.5f

            // Column Widths
            val wNo = 45f
            val wDesc = 140f
            val wBirds = 105f
            val wWeight = 115f
            val wAvg = 118f

            val xNo = MARGIN + (wNo / 2)
            val xDesc = MARGIN + wNo + 12f
            val xBirds = MARGIN + wNo + wDesc + wBirds - 14f
            val xWeight = MARGIN + wNo + wDesc + wBirds + wWeight - 14f
            val xAvg = MARGIN + CONTENT_WIDTH - 14f

            boldPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("NO", xNo, tableY + 14.5f, boldPaint)

            boldPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("SESI TIMBANGAN", xDesc, tableY + 14.5f, boldPaint)

            boldPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("JUMLAH EKOR", xBirds, tableY + 14.5f, boldPaint)
            canvas.drawText("BERAT NETTO (KG)", xWeight, tableY + 14.5f, boldPaint)
            canvas.drawText("RATA² / EKOR (KG)", xAvg, tableY + 14.5f, boldPaint)

            currentY += headerHeight

            // Table Rows
            val startIndex = pageIndex * rowsPerPage
            val endIndex = (startIndex + rowsPerPage).coerceAtMost(totalEntries)
            val rowHeight = 18f

            textPaint.textSize = 8.5f

            for (i in startIndex until endIndex) {
                val entry = ticket.entries[i]
                val rowY = currentY

                // Zebra row striping
                fillPaint.color = if (i % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(MARGIN, rowY, MARGIN + CONTENT_WIDTH, rowY + rowHeight, fillPaint)

                // Row border bottom
                linePaint.color = Color.rgb(226, 232, 240)
                canvas.drawLine(MARGIN, rowY + rowHeight, MARGIN + CONTENT_WIDTH, rowY + rowHeight, linePaint)

                textPaint.color = Color.rgb(30, 41, 59)

                // No.
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText("${entry.scaleNumber}", xNo, rowY + 12.5f, textPaint)

                // Sesi Description
                textPaint.textAlign = Paint.Align.LEFT
                canvas.drawText("Timbangan Keranjang #${entry.scaleNumber}", xDesc, rowY + 12.5f, textPaint)

                // Ekor
                textPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("${entry.birdCount} ekor", xBirds, rowY + 12.5f, textPaint)

                // Berat (Kg)
                canvas.drawText("%.1f kg".format(entry.netWeightKg), xWeight, rowY + 12.5f, textPaint)

                // Rata-rata (Kg)
                textPaint.color = Color.rgb(2, 132, 199) // Sky Blue 600
                canvas.drawText("%.2f kg".format(entry.avgWeightKg), xAvg, rowY + 12.5f, textPaint)

                currentY += rowHeight
            }

            // Outline around table
            linePaint.color = Color.rgb(148, 163, 184)
            canvas.drawRect(MARGIN, tableY, MARGIN + CONTENT_WIDTH, currentY, linePaint)

            // 4. Grand Total Row (Only on the last page)
            if (pageIndex == totalPages - 1) {
                val totalRowHeight = 26f
                fillPaint.color = Color.rgb(209, 250, 229) // Emerald 100
                canvas.drawRect(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + totalRowHeight, fillPaint)

                linePaint.color = Color.rgb(16, 185, 129) // Emerald 500
                linePaint.strokeWidth = 1.5f
                canvas.drawRect(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + totalRowHeight, linePaint)
                linePaint.strokeWidth = 1f

                boldPaint.color = Color.rgb(6, 78, 59) // Deep Emerald
                boldPaint.textSize = 9.5f

                boldPaint.textAlign = Paint.Align.LEFT
                canvas.drawText("TOTAL AKUMULASI PANEN", MARGIN + 12f, currentY + 16.5f, boldPaint)

                boldPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("${ticket.totalBirdCount} Ekor", xBirds, currentY + 16.5f, boldPaint)
                canvas.drawText("%.2f Kg".format(ticket.totalWeightKg), xWeight, currentY + 16.5f, boldPaint)
                canvas.drawText("%.2f Kg/ekor".format(ticket.overallAvgWeightKg), xAvg, currentY + 16.5f, boldPaint)

                currentY += totalRowHeight + 14f

                // Notes if any
                if (ticket.notes.isNotBlank()) {
                    textPaint.color = Color.rgb(100, 116, 139)
                    textPaint.textSize = 8.5f
                    textPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText("Catatan Tambahan: ${ticket.notes}", MARGIN + 4f, currentY + 10f, textPaint)
                    currentY += 18f
                }

                // 5. Official 3-Party Signatures Box
                val sigY = currentY + 10f
                val sigBoxWidth = CONTENT_WIDTH / 3

                boldPaint.textSize = 9f
                boldPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 8.5f
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.color = Color.rgb(71, 85, 105)

                // Party 1: Peternak
                val xParty1 = MARGIN + (sigBoxWidth * 0.5f)
                canvas.drawText("Pihak Peternak,", xParty1, sigY, textPaint)
                canvas.drawText("PROFIT Broiler Club", xParty1, sigY + 12f, boldPaint)
                canvas.drawLine(xParty1 - 65f, sigY + 54f, xParty1 + 65f, sigY + 54f, linePaint)
                canvas.drawText("(${ticket.farmerName.substringBefore(" /")})", xParty1, sigY + 66f, textPaint)

                // Party 2: Driver Armada
                val xParty2 = MARGIN + (sigBoxWidth * 1.5f)
                canvas.drawText("Pengemudi / Driver,", xParty2, sigY, textPaint)
                canvas.drawText("Armada Pengangkut", xParty2, sigY + 12f, boldPaint)
                canvas.drawLine(xParty2 - 65f, sigY + 54f, xParty2 + 65f, sigY + 54f, linePaint)
                canvas.drawText("(${ticket.driverName})", xParty2, sigY + 66f, textPaint)

                // Party 3: Pembeli / Bakul
                val xParty3 = MARGIN + (sigBoxWidth * 2.5f)
                canvas.drawText("Pihak Pembeli,", xParty3, sigY, textPaint)
                canvas.drawText("Penerima Ayam", xParty3, sigY + 12f, boldPaint)
                canvas.drawLine(xParty3 - 65f, sigY + 54f, xParty3 + 65f, sigY + 54f, linePaint)
                canvas.drawText("(${ticket.buyerName.substringBefore(" (")})", xParty3, sigY + 66f, textPaint)
            }

            // Page Footer
            textPaint.color = Color.rgb(148, 163, 184)
            textPaint.textSize = 8f
            textPaint.textAlign = Paint.Align.LEFT
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("in", "ID"))
            canvas.drawText("Dokumen sah diterbitkan via PROFIT Broiler Club Smart Poultry App • Dicetak: ${dateFormat.format(Date())}", MARGIN, PAGE_HEIGHT - 22f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Hal ${pageIndex + 1} dari $totalPages", MARGIN + CONTENT_WIDTH, PAGE_HEIGHT - 22f, textPaint)

            pdfDocument.finishPage(page)
        }

        // Save file to cache directory
        val fileName = "Surat_Jalan_Panen_${ticket.ticketNumber.replace("-", "_")}.pdf"
        val pdfFile = File(context.cacheDir, fileName)

        try {
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return pdfFile
    }

    /**
     * Share the generated PDF file to WhatsApp, Telegram, Email, Google Drive, or Bluetooth Printer.
     */
    fun sharePdf(context: Context, pdfFile: File, ticket: HarvestTicket) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Surat Jalan & Timbangan Panen - ${ticket.ticketNumber}")
                putExtra(Intent.EXTRA_TEXT, "Berikut kami lampirkan Dokumen PDF Resmi Hasil Timbangan Panen Ayam Broiler (${ticket.ticketNumber}) armada ${ticket.driverName} (${ticket.vehiclePlate}).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Dokumen PDF Panen"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Open or print the generated PDF document directly using Android's PDF viewer / Print Spooler.
     */
    fun openPdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Buka / Cetak Dokumen PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Tidak ada aplikasi pembuka PDF di perangkat ini. Anda dapat membagikannya via WhatsApp.", Toast.LENGTH_LONG).show()
        }
    }
}
