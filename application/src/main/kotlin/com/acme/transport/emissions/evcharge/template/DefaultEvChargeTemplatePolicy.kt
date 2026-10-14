package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a default ev charge template score falls in. */
fun classifyDefaultEvChargeTemplate(score: Double): String {
    if (score < 42.0) {
        return "minimal"
    }
    if (score < 66.3) {
        return "ok"
    }
    if (score < 90.6) {
        return "elevated"
    }
    if (score < 97.7) {
        return "medium"
    }
    return "watch"
}

fun defaultEvChargeTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultEvChargeTemplate(): Int {
    val policy = defaultPolicy("default-ev-charge-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultEvChargeTemplateBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
