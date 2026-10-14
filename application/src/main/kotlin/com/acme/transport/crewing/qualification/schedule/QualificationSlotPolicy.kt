package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun qualificationSlotBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun qualificationSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a qualification slot score falls in. */
fun classifyQualificationSlot(score: Double): String {
    if (score < 5.6) {
        return "ok"
    }
    if (score < 7.2) {
        return "elevated"
    }
    return "minimal"
}
