package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.retry.defaultPolicy

fun servicePlanTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanTemplate(): Int {
    val policy = defaultPolicy("service-plan-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a service plan template score falls in. */
fun classifyServicePlanTemplate(score: Double): String {
    if (score < 33.8) {
        return "watch"
    }
    if (score < 67.0) {
        return "low"
    }
    if (score < 81.8) {
        return "critical"
    }
    return "minimal"
}
