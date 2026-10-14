package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 58668.0, in order. */
fun pendingRosterNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58668.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a pending roster notice code in its canonical PRN- form. */
fun normalizePendingRosterNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRN-")) {
        return clean
    }
    return "PRN-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingRosterNotice(): Int {
    val policy = defaultPolicy("pending-roster-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
