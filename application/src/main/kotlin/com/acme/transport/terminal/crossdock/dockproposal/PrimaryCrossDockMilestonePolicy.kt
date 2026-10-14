package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a primary cross dock milestone score falls in. */
fun classifyPrimaryCrossDockMilestone(score: Double): String {
    if (score < 30.1) {
        return "minimal"
    }
    if (score < 84.2) {
        return "medium"
    }
    return "watch"
}

fun mergePrimaryCrossDockMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryCrossDockMilestone(): Int {
    val policy = defaultPolicy("primary-cross-dock-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
