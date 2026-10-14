package com.acme.transport.freightpay.audit.mapping

fun seasonalAuditScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a seasonal audit schedule code in its canonical SAS- form. */
fun normalizeSeasonalAuditScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAS-")) {
        return clean
    }
    return "SAS-${clean.replace(" ", "-")}"
}
