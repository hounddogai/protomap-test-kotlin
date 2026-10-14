package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 72297.0, in order. */
fun primaryDutyPeriodDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72297.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary duty period draft code in its canonical PDPD- form. */
fun normalizePrimaryDutyPeriodDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDPD-")) {
        return clean
    }
    return "PDPD-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryDutyPeriodDraft(): Int {
    val policy = defaultPolicy("primary-duty-period-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
