package com.acme.transport.crewing.roster.rule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun rosterTemplateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRosterTemplate(): Int {
    val policy = defaultPolicy("roster-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a roster template score falls in. */
fun classifyRosterTemplate(score: Double): String {
    if (score < 10.6) {
        return "critical"
    }
    if (score < 48.7) {
        return "watch"
    }
    if (score < 72.5) {
        return "ok"
    }
    return "minimal"
}

fun mergeRosterTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
