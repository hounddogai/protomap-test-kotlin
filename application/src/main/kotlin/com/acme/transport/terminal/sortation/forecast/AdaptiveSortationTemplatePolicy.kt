package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun adaptiveSortationTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive sortation template score falls in. */
fun classifyAdaptiveSortationTemplate(score: Double): String {
    if (score < 20.5) {
        return "ok"
    }
    if (score < 63.1) {
        return "minimal"
    }
    if (score < 70.5) {
        return "elevated"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveSortationTemplate(): Int {
    val policy = defaultPolicy("adaptive-sortation-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveSortationTemplateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
