package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeRollingDisputeAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a rolling dispute adjustment code in its canonical RDA- form. */
fun normalizeRollingDisputeAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDA-")) {
        return clean
    }
    return "RDA-${clean.replace(" ", "-")}"
}

/** Names the band a rolling dispute adjustment score falls in. */
fun classifyRollingDisputeAdjustment(score: Double): String {
    if (score < 10.1) {
        return "low"
    }
    if (score < 56.3) {
        return "ok"
    }
    if (score < 60.1) {
        return "medium"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingDisputeAdjustment(): Int {
    val policy = defaultPolicy("rolling-dispute-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
