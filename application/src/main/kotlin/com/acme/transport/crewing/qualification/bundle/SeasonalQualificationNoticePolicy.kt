package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalQualificationNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a seasonal qualification notice score falls in. */
fun classifySeasonalQualificationNotice(score: Double): String {
    if (score < 6.0) {
        return "watch"
    }
    if (score < 91.4) {
        return "ok"
    }
    if (score < 98.0) {
        return "minimal"
    }
    return "medium"
}
