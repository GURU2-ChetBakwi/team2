package com.guru2.chetbakwi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor

class PDFHelper(private val context: Context, private val uri: Uri) {
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: Int = 0

    fun openPdfRenderer() {
        val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        pdfRenderer = fileDescriptor?.let { PdfRenderer(it) }
    }

    fun getPageCount(): Int {
        return pdfRenderer?.pageCount ?: 0
    }

    fun getBitmapFromPage(page: Int): Bitmap? {
        if (page < 0 || page >= getPageCount()) return null
        val pdfPage = pdfRenderer?.openPage(page)
        val bitmap = pdfPage?.let {
            Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888)
        }
        pdfPage?.render(bitmap!!, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        pdfPage?.close()
        return bitmap
    }

    fun closePdfRenderer() {
        pdfRenderer?.close()
    }
}
