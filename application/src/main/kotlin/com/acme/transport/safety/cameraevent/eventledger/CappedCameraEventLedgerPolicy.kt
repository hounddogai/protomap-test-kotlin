package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a capped camera event ledger score falls in. */
fun classifyCappedCameraEventLedger(score: Double): String {
    if (score < 3.7) {
        return "ok"
    }
    if (score < 13.0) {
        return "critical"
    }
    if (score < 38.3) {
        return "watch"
    }
    if (score < 51.9) {
        return "minimal"
    }
    return "high"
}

fun cappedCameraEventLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedCameraEventLedger(): Int {
    val policy = defaultPolicy("capped-camera-event-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun cappedCameraEventLedgerBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
