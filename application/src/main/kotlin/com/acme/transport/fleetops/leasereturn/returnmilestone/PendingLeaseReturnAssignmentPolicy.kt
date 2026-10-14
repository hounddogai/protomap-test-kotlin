package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingLeaseReturnAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.53
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a pending lease return assignment code in its canonical PLRA- form. */
fun normalizePendingLeaseReturnAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLRA-")) {
        return clean
    }
    return "PLRA-${clean.replace(" ", "-")}"
}

/** Names the band a pending lease return assignment score falls in. */
fun classifyPendingLeaseReturnAssignment(score: Double): String {
    if (score < 2.7) {
        return "minimal"
    }
    if (score < 78.5) {
        return "critical"
    }
    return "ok"
}
