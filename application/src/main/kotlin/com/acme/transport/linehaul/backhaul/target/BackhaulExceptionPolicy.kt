package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a backhaul exception score falls in. */
fun classifyBackhaulException(score: Double): String {
    if (score < 20.1) {
        return "elevated"
    }
    if (score < 28.9) {
        return "watch"
    }
    if (score < 38.6) {
        return "minimal"
    }
    return "medium"
}

fun mergeBackhaulExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun backhaulExceptionBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
