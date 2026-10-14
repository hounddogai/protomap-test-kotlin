package com.acme.transport.fleetops.leasereturn.schedule

/** Puts a lease return schedule code in its canonical LRS- form. */
fun normalizeLeaseReturnScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRS-")) {
        return clean
    }
    return "LRS-${clean.replace(" ", "-")}"
}

fun leaseReturnScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
