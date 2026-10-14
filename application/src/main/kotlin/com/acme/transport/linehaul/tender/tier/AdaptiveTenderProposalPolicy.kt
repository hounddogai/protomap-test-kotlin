package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveTenderProposal(): Int {
    val policy = defaultPolicy("adaptive-tender-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveTenderProposalBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun adaptiveTenderProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 25541.0, in order. */
fun adaptiveTenderProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25541.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
