package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSortationException(): Int {
    val policy = defaultPolicy("sortation-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun sortationExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a sortation exception score falls in. */
fun classifySortationException(score: Double): String {
    if (score < 3.2) {
        return "ok"
    }
    if (score < 20.5) {
        return "minimal"
    }
    if (score < 25.7) {
        return "medium"
    }
    return "watch"
}

fun mergeSortationExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
