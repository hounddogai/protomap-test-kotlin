package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a cross dock audit score falls in. */
fun classifyCrossDockAudit(score: Double): String {
    if (score < 4.3) {
        return "minimal"
    }
    if (score < 73.6) {
        return "ok"
    }
    if (score < 95.5) {
        return "critical"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 4307.0, in order. */
fun crossDockAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4307.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCrossDockAudit(): Int {
    val policy = defaultPolicy("cross-dock-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
