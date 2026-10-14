package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Puts a adaptive hub milestone code in its canonical AHM- form. */
fun normalizeAdaptiveHubMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AHM-")) {
        return clean
    }
    return "AHM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveHubMilestone(): Int {
    val policy = defaultPolicy("adaptive-hub-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 6751.0, in order. */
fun adaptiveHubMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6751.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeAdaptiveHubMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
