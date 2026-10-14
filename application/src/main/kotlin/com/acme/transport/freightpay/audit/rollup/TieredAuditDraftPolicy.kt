package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredAuditDraft(): Int {
    val policy = defaultPolicy("tiered-audit-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a tiered audit draft code in its canonical TAD- form. */
fun normalizeTieredAuditDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TAD-")) {
        return clean
    }
    return "TAD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 6613.0, in order. */
fun tieredAuditDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6613.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
