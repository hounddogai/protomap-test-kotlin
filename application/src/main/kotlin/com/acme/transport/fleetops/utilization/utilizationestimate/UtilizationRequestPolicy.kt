package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeUtilizationRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a utilization request code in its canonical UR- form. */
fun normalizeUtilizationRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("UR-")) {
        return clean
    }
    return "UR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeUtilizationRequest(): Int {
    val policy = defaultPolicy("utilization-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a utilization request score falls in. */
fun classifyUtilizationRequest(score: Double): String {
    if (score < 24.3) {
        return "low"
    }
    if (score < 40.2) {
        return "elevated"
    }
    return "ok"
}
