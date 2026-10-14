package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun carbonReportRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun carbonReportRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a carbon report request score falls in. */
fun classifyCarbonReportRequest(score: Double): String {
    if (score < 30.6) {
        return "high"
    }
    if (score < 62.9) {
        return "watch"
    }
    if (score < 81.9) {
        return "medium"
    }
    return "critical"
}
