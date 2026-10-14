package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a regional qualification policy score falls in. */
fun classifyRegionalQualificationPolicy(score: Double): String {
    if (score < 23.6) {
        return "minimal"
    }
    if (score < 63.6) {
        return "medium"
    }
    if (score < 67.4) {
        return "elevated"
    }
    return "critical"
}

fun regionalQualificationPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
