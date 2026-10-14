package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun legacyFuelSurchargeSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a legacy fuel surcharge snapshot score falls in. */
fun classifyLegacyFuelSurchargeSnapshot(score: Double): String {
    if (score < 12.2) {
        return "high"
    }
    if (score < 50.5) {
        return "critical"
    }
    if (score < 88.3) {
        return "minimal"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyFuelSurchargeSnapshot(): Int {
    val policy = defaultPolicy("legacy-fuel-surcharge-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun legacyFuelSurchargeSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
