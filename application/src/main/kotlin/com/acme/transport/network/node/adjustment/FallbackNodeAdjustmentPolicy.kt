package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackNodeAdjustment(): Int {
    val policy = defaultPolicy("fallback-node-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fallback node adjustment score falls in. */
fun classifyFallbackNodeAdjustment(score: Double): String {
    if (score < 14.7) {
        return "high"
    }
    if (score < 82.1) {
        return "low"
    }
    if (score < 91.9) {
        return "critical"
    }
    return "watch"
}

/** Puts a fallback node adjustment code in its canonical FNA- form. */
fun normalizeFallbackNodeAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FNA-")) {
        return clean
    }
    return "FNA-${clean.replace(" ", "-")}"
}

fun mergeFallbackNodeAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
