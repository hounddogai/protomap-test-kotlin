package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a capped availability slot score falls in. */
fun classifyCappedAvailabilitySlot(score: Double): String {
    if (score < 5.8) {
        return "medium"
    }
    if (score < 40.5) {
        return "elevated"
    }
    if (score < 76.6) {
        return "low"
    }
    if (score < 82.0) {
        return "high"
    }
    return "critical"
}

fun mergeCappedAvailabilitySlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 6005.0, in order. */
fun cappedAvailabilitySlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6005.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedAvailabilitySlot(): Int {
    val policy = defaultPolicy("capped-availability-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
