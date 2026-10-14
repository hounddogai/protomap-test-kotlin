package com.acme.transport.lastmile.redelivery.redeliveryentry

/** Puts a seasonal redelivery reservation code in its canonical SRR- form. */
fun normalizeSeasonalRedeliveryReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRR-")) {
        return clean
    }
    return "SRR-${clean.replace(" ", "-")}"
}

fun seasonalRedeliveryReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
