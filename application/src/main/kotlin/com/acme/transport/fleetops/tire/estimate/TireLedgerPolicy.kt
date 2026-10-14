package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tireLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a tire ledger code in its canonical TL- form. */
fun normalizeTireLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TL-")) {
        return clean
    }
    return "TL-${clean.replace(" ", "-")}"
}
