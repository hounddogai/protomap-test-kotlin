package com.acme.transport.fleetops.leasereturn.mapping

/** Puts a manual lease return entry code in its canonical MLRE- form. */
fun normalizeManualLeaseReturnEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MLRE-")) {
        return clean
    }
    return "MLRE-${clean.replace(" ", "-")}"
}

fun manualLeaseReturnEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
