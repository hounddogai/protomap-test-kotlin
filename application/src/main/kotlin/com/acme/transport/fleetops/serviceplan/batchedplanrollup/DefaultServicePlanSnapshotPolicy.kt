package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultServicePlanSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a default service plan snapshot code in its canonical DSPS- form. */
fun normalizeDefaultServicePlanSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DSPS-")) {
        return clean
    }
    return "DSPS-${clean.replace(" ", "-")}"
}
