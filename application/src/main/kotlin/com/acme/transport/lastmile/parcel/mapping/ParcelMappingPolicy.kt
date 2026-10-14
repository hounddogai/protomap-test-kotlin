package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.numbers.roundTo

fun parcelMappingBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a parcel mapping code in its canonical PM- form. */
fun normalizeParcelMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PM-")) {
        return clean
    }
    return "PM-${clean.replace(" ", "-")}"
}

fun parcelMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
