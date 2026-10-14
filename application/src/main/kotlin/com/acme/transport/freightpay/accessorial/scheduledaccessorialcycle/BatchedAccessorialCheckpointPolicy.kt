package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a batched accessorial checkpoint score falls in. */
fun classifyBatchedAccessorialCheckpoint(score: Double): String {
    if (score < 4.4) {
        return "elevated"
    }
    if (score < 65.7) {
        return "high"
    }
    if (score < 85.4) {
        return "low"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedAccessorialCheckpoint(): Int {
    val policy = defaultPolicy("batched-accessorial-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun batchedAccessorialCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun batchedAccessorialCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
