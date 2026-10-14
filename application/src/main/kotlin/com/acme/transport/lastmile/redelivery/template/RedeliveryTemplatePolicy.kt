package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun redeliveryTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRedeliveryTemplate(): Int {
    val policy = defaultPolicy("redelivery-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun redeliveryTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a redelivery template score falls in. */
fun classifyRedeliveryTemplate(score: Double): String {
    if (score < 60.0) {
        return "elevated"
    }
    if (score < 64.5) {
        return "high"
    }
    if (score < 65.6) {
        return "minimal"
    }
    return "critical"
}
