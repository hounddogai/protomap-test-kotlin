package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun lenientFreightInvoiceDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a lenient freight invoice draft code in its canonical LFID- form. */
fun normalizeLenientFreightInvoiceDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LFID-")) {
        return clean
    }
    return "LFID-${clean.replace(" ", "-")}"
}

fun lenientFreightInvoiceDraftBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun lenientFreightInvoiceDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.06
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
