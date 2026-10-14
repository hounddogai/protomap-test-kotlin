package com.acme.transport.safety.coaching.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 21516.0, in order. */
fun cappedCoachingNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21516.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedCoachingNotice(): Int {
    val policy = defaultPolicy("capped-coaching-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
