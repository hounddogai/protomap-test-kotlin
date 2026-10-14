package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun weightedTariffPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedTariffPolicy(): Int {
    val policy = defaultPolicy("weighted-tariff-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a weighted tariff policy code in its canonical WTP- form. */
fun normalizeWeightedTariffPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WTP-")) {
        return clean
    }
    return "WTP-${clean.replace(" ", "-")}"
}
