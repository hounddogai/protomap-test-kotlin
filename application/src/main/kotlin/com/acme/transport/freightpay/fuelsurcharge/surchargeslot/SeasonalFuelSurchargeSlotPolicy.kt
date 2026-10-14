package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalFuelSurchargeSlot(): Int {
    val policy = defaultPolicy("seasonal-fuel-surcharge-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeSeasonalFuelSurchargeSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a seasonal fuel surcharge slot score falls in. */
fun classifySeasonalFuelSurchargeSlot(score: Double): String {
    if (score < 28.0) {
        return "critical"
    }
    if (score < 40.1) {
        return "high"
    }
    if (score < 96.3) {
        return "ok"
    }
    return "minimal"
}
