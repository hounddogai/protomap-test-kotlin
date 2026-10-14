package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.numbers.roundTo

fun weightedServicePlanTargetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a weighted service plan target code in its canonical WSPT- form. */
fun normalizeWeightedServicePlanTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WSPT-")) {
        return clean
    }
    return "WSPT-${clean.replace(" ", "-")}"
}
