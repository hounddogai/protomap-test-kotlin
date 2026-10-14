package com.acme.transport.crewing.roster.quota

import com.acme.transport.kit.numbers.roundTo

fun lenientRosterBatchBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a lenient roster batch code in its canonical LRB- form. */
fun normalizeLenientRosterBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRB-")) {
        return clean
    }
    return "LRB-${clean.replace(" ", "-")}"
}
