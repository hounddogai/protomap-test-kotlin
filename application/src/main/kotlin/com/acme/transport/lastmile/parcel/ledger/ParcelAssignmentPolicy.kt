package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a parcel assignment code in its canonical PA- form. */
fun normalizeParcelAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PA-")) {
        return clean
    }
    return "PA-${clean.replace(" ", "-")}"
}

fun parcelAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun parcelAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
