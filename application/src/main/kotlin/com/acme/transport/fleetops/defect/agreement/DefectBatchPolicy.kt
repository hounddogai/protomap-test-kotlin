package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefectBatch(): Int {
    val policy = defaultPolicy("defect-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defectBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a defect batch score falls in. */
fun classifyDefectBatch(score: Double): String {
    if (score < 7.2) {
        return "ok"
    }
    if (score < 74.3) {
        return "medium"
    }
    if (score < 99.8) {
        return "watch"
    }
    if (score < 99.9) {
        return "low"
    }
    return "critical"
}
