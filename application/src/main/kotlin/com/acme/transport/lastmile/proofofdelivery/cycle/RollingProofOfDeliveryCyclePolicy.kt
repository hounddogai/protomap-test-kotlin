package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun rollingProofOfDeliveryCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.4
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rollingProofOfDeliveryCycleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a rolling proof of delivery cycle code in its canonical RPODC- form. */
fun normalizeRollingProofOfDeliveryCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RPODC-")) {
        return clean
    }
    return "RPODC-${clean.replace(" ", "-")}"
}
