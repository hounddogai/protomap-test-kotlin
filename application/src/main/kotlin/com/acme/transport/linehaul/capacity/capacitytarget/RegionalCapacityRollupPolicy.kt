package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeRegionalCapacityRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a regional capacity rollup code in its canonical RCR- form. */
fun normalizeRegionalCapacityRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCR-")) {
        return clean
    }
    return "RCR-${clean.replace(" ", "-")}"
}

/** Names the band a regional capacity rollup score falls in. */
fun classifyRegionalCapacityRollup(score: Double): String {
    if (score < 34.6) {
        return "medium"
    }
    if (score < 57.8) {
        return "minimal"
    }
    if (score < 68.3) {
        return "critical"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalCapacityRollup(): Int {
    val policy = defaultPolicy("regional-capacity-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
