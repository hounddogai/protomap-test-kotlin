package com.acme.transport.linehaul.laneplan.planagreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a primary lane plan draft score falls in. */
fun classifyPrimaryLanePlanDraft(score: Double): String {
    if (score < 9.9) {
        return "high"
    }
    if (score < 12.4) {
        return "low"
    }
    if (score < 90.3) {
        return "watch"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryLanePlanDraft(): Int {
    val policy = defaultPolicy("primary-lane-plan-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergePrimaryLanePlanDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun primaryLanePlanDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
