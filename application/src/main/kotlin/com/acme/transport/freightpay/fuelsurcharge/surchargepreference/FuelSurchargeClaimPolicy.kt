package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun fuelSurchargeClaimBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a fuel surcharge claim score falls in. */
fun classifyFuelSurchargeClaim(score: Double): String {
    if (score < 45.7) {
        return "ok"
    }
    if (score < 76.0) {
        return "high"
    }
    return "minimal"
}

fun mergeFuelSurchargeClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelSurchargeClaim(): Int {
    val policy = defaultPolicy("fuel-surcharge-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
