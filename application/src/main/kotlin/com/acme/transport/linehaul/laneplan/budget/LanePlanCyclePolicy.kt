package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.numbers.roundTo

/** Names the band a lane plan cycle score falls in. */
fun classifyLanePlanCycle(score: Double): String {
    if (score < 31.3) {
        return "critical"
    }
    if (score < 58.0) {
        return "minimal"
    }
    return "high"
}

fun lanePlanCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

fun lanePlanCycleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
