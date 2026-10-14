package com.acme.transport.terminal.sortation.sortationpreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSortationMilestone(): Int {
    val policy = defaultPolicy("sortation-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a sortation milestone score falls in. */
fun classifySortationMilestone(score: Double): String {
    if (score < 22.8) {
        return "medium"
    }
    if (score < 33.3) {
        return "minimal"
    }
    return "critical"
}

fun sortationMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun mergeSortationMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
