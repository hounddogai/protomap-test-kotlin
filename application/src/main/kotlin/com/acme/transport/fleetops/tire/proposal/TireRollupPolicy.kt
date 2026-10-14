package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a tire rollup code in its canonical TR- form. */
fun normalizeTireRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TR-")) {
        return clean
    }
    return "TR-${clean.replace(" ", "-")}"
}

fun tireRollupBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun tireRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.0
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
