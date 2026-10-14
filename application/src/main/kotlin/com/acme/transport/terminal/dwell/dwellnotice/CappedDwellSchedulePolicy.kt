package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun cappedDwellScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a capped dwell schedule score falls in. */
fun classifyCappedDwellSchedule(score: Double): String {
    if (score < 3.7) {
        return "watch"
    }
    if (score < 35.3) {
        return "low"
    }
    if (score < 73.0) {
        return "ok"
    }
    return "elevated"
}

/** Puts a capped dwell schedule code in its canonical CDS- form. */
fun normalizeCappedDwellScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDS-")) {
        return clean
    }
    return "CDS-${clean.replace(" ", "-")}"
}

fun cappedDwellScheduleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
