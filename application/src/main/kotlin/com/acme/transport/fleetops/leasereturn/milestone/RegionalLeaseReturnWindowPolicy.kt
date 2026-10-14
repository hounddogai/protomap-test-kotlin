package com.acme.transport.fleetops.leasereturn.milestone

/** Puts a regional lease return window code in its canonical RLRW- form. */
fun normalizeRegionalLeaseReturnWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RLRW-")) {
        return clean
    }
    return "RLRW-${clean.replace(" ", "-")}"
}

fun regionalLeaseReturnWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
