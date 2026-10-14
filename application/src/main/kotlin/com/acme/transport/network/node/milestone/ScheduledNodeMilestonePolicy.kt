package com.acme.transport.network.node.milestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledNodeMilestone(): Int {
    val policy = defaultPolicy("scheduled-node-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledNodeMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a scheduled node milestone code in its canonical SNM- form. */
fun normalizeScheduledNodeMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SNM-")) {
        return clean
    }
    return "SNM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 37440.0, in order. */
fun scheduledNodeMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37440.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
