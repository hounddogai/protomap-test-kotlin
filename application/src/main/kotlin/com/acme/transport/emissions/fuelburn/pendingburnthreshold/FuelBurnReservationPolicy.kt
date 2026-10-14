package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a fuel burn reservation score falls in. */
fun classifyFuelBurnReservation(score: Double): String {
    if (score < 44.8) {
        return "critical"
    }
    if (score < 58.0) {
        return "medium"
    }
    return "ok"
}

/** Puts a fuel burn reservation code in its canonical FBR- form. */
fun normalizeFuelBurnReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBR-")) {
        return clean
    }
    return "FBR-${clean.replace(" ", "-")}"
}

fun fuelBurnReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun mergeFuelBurnReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
