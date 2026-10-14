package com.acme.transport.crewing.qualification.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeCappedQualificationSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capped qualification segment score falls in. */
fun classifyCappedQualificationSegment(score: Double): String {
    if (score < 43.9) {
        return "high"
    }
    if (score < 83.1) {
        return "elevated"
    }
    if (score < 89.1) {
        return "watch"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedQualificationSegment(): Int {
    val policy = defaultPolicy("capped-qualification-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun cappedQualificationSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
