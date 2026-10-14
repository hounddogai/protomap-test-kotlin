package com.acme.transport.crewing.qualification.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun qualificationTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeQualificationTemplate(): Int {
    val policy = defaultPolicy("qualification-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeQualificationTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a qualification template score falls in. */
fun classifyQualificationTemplate(score: Double): String {
    if (score < 35.7) {
        return "low"
    }
    if (score < 53.4) {
        return "medium"
    }
    if (score < 61.3) {
        return "critical"
    }
    if (score < 87.9) {
        return "watch"
    }
    return "ok"
}
