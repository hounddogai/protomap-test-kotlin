package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.kit.numbers.roundTo

fun legacyLeaseReturnEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Puts a legacy lease return entry code in its canonical LLRE- form. */
fun normalizeLegacyLeaseReturnEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LLRE-")) {
        return clean
    }
    return "LLRE-${clean.replace(" ", "-")}"
}

fun legacyLeaseReturnEntryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
