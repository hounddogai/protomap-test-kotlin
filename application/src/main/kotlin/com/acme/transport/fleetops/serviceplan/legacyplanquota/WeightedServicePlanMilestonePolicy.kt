package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a weighted service plan milestone code in its canonical WSPM- form. */
fun normalizeWeightedServicePlanMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WSPM-")) {
        return clean
    }
    return "WSPM-${clean.replace(" ", "-")}"
}

fun weightedServicePlanMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
