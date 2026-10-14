package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledRedeliveryTemplate(): Int {
    val policy = defaultPolicy("scheduled-redelivery-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a scheduled redelivery template score falls in. */
fun classifyScheduledRedeliveryTemplate(score: Double): String {
    if (score < 43.4) {
        return "minimal"
    }
    if (score < 80.5) {
        return "ok"
    }
    if (score < 86.8) {
        return "watch"
    }
    return "elevated"
}

fun mergeScheduledRedeliveryTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
