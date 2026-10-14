package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a fallback freight invoice manifest code in its canonical FFIM- form. */
fun normalizeFallbackFreightInvoiceManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FFIM-")) {
        return clean
    }
    return "FFIM-${clean.replace(" ", "-")}"
}

fun fallbackFreightInvoiceManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
