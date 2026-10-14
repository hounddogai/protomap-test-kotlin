package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a service plan batch code in its canonical SPB- form. */
fun normalizeServicePlanBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPB-")) {
        return clean
    }
    return "SPB-${clean.replace(" ", "-")}"
}

fun servicePlanBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
