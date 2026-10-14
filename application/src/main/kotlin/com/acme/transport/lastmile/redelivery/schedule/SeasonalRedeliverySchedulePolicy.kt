package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun seasonalRedeliveryScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun seasonalRedeliveryScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a seasonal redelivery schedule score falls in. */
fun classifySeasonalRedeliverySchedule(score: Double): String {
    if (score < 22.9) {
        return "minimal"
    }
    if (score < 93.0) {
        return "elevated"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalRedeliverySchedule(): Int {
    val policy = defaultPolicy("seasonal-redelivery-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
