package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a strict defect slot score falls in. */
fun classifyStrictDefectSlot(score: Double): String {
    if (score < 41.2) {
        return "medium"
    }
    if (score < 44.9) {
        return "elevated"
    }
    return "ok"
}

fun mergeStrictDefectSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictDefectSlot(): Int {
    val policy = defaultPolicy("strict-defect-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 73967.0, in order. */
fun strictDefectSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 73967.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
