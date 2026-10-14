package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a regional service area reservation code in its canonical RSAR- form. */
fun normalizeRegionalServiceAreaReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RSAR-")) {
        return clean
    }
    return "RSAR-${clean.replace(" ", "-")}"
}

/** Names the band a regional service area reservation score falls in. */
fun classifyRegionalServiceAreaReservation(score: Double): String {
    if (score < 89.7) {
        return "high"
    }
    if (score < 96.3) {
        return "medium"
    }
    return "elevated"
}

fun mergeRegionalServiceAreaReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
