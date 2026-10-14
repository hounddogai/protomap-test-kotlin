package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a qualification bundle score falls in. */
fun classifyQualificationBundle(score: Double): String {
    if (score < 61.3) {
        return "minimal"
    }
    if (score < 61.7) {
        return "medium"
    }
    if (score < 88.2) {
        return "high"
    }
    return "ok"
}

fun qualificationBundleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeQualificationBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
