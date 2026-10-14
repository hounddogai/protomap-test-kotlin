package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun cappedServicePlanClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.92
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a capped service plan claim score falls in. */
fun classifyCappedServicePlanClaim(score: Double): String {
    if (score < 4.2) {
        return "minimal"
    }
    if (score < 30.9) {
        return "ok"
    }
    if (score < 38.7) {
        return "medium"
    }
    return "watch"
}

/** Puts a capped service plan claim code in its canonical CSPC- form. */
fun normalizeCappedServicePlanClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CSPC-")) {
        return clean
    }
    return "CSPC-${clean.replace(" ", "-")}"
}

fun cappedServicePlanClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
