package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 27339.0, in order. */
fun lenientAccessorialProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 27339.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientAccessorialProposal(): Int {
    val policy = defaultPolicy("lenient-accessorial-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
