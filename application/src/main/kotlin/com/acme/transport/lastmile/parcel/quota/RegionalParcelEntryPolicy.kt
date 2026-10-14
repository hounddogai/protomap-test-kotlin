package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.numbers.roundTo

fun regionalParcelEntryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a regional parcel entry code in its canonical RPE- form. */
fun normalizeRegionalParcelEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RPE-")) {
        return clean
    }
    return "RPE-${clean.replace(" ", "-")}"
}
