package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun violationAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a violation assignment score falls in. */
fun classifyViolationAssignment(score: Double): String {
    if (score < 2.7) {
        return "low"
    }
    if (score < 36.1) {
        return "medium"
    }
    if (score < 49.0) {
        return "minimal"
    }
    return "ok"
}
