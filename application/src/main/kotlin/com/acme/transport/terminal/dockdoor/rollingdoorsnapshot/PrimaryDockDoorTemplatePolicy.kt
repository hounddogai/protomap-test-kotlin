package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryDockDoorTemplate(): Int {
    val policy = defaultPolicy("primary-dock-door-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun primaryDockDoorTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun mergePrimaryDockDoorTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a primary dock door template score falls in. */
fun classifyPrimaryDockDoorTemplate(score: Double): String {
    if (score < 15.0) {
        return "critical"
    }
    if (score < 16.0) {
        return "low"
    }
    if (score < 76.0) {
        return "minimal"
    }
    if (score < 87.9) {
        return "medium"
    }
    return "ok"
}
