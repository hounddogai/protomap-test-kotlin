package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun strictCrossDockAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a strict cross dock agreement code in its canonical SCDA- form. */
fun normalizeStrictCrossDockAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCDA-")) {
        return clean
    }
    return "SCDA-${clean.replace(" ", "-")}"
}

fun strictCrossDockAgreementBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
