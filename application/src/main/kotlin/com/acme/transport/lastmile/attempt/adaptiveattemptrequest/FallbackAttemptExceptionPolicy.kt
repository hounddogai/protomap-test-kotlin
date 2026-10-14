package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeFallbackAttemptExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a fallback attempt exception score falls in. */
fun classifyFallbackAttemptException(score: Double): String {
    if (score < 20.2) {
        return "watch"
    }
    if (score < 20.8) {
        return "high"
    }
    return "ok"
}
