package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeWeightedDeclarationDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a weighted declaration digest score falls in. */
fun classifyWeightedDeclarationDigest(score: Double): String {
    if (score < 40.4) {
        return "elevated"
    }
    if (score < 41.6) {
        return "watch"
    }
    return "ok"
}

fun weightedDeclarationDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun weightedDeclarationDigestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
