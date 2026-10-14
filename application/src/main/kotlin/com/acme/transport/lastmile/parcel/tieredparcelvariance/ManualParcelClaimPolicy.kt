package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualParcelClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a manual parcel claim score falls in. */
fun classifyManualParcelClaim(score: Double): String {
    if (score < 55.2) {
        return "low"
    }
    if (score < 64.0) {
        return "watch"
    }
    return "medium"
}
