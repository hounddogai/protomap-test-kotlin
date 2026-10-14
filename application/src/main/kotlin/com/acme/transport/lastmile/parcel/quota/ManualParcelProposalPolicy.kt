package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a manual parcel proposal code in its canonical MPP- form. */
fun normalizeManualParcelProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MPP-")) {
        return clean
    }
    return "MPP-${clean.replace(" ", "-")}"
}

fun manualParcelProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
