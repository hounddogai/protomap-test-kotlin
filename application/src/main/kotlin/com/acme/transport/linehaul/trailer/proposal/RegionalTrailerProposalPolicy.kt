package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalTrailerProposal(): Int {
    val policy = defaultPolicy("regional-trailer-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 68929.0, in order. */
fun regionalTrailerProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68929.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalTrailerProposalBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
