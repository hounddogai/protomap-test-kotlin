package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a global audit proposal score falls in. */
fun classifyGlobalAuditProposal(score: Double): String {
    if (score < 19.9) {
        return "medium"
    }
    if (score < 81.3) {
        return "critical"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalAuditProposal(): Int {
    val policy = defaultPolicy("global-audit-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 65638.0, in order. */
fun globalAuditProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 65638.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
