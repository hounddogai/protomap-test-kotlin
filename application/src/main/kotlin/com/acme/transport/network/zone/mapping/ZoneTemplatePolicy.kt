package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 43968.0, in order. */
fun zoneTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43968.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a zone template score falls in. */
fun classifyZoneTemplate(score: Double): String {
    if (score < 21.0) {
        return "critical"
    }
    if (score < 74.1) {
        return "high"
    }
    if (score < 89.0) {
        return "watch"
    }
    if (score < 98.0) {
        return "medium"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeZoneTemplate(): Int {
    val policy = defaultPolicy("zone-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeZoneTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
