package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 48673.0, in order. */
fun primaryNearMissBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48673.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryNearMissBudget(): Int {
    val policy = defaultPolicy("primary-near-miss-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a primary near miss budget code in its canonical PNMB- form. */
fun normalizePrimaryNearMissBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PNMB-")) {
        return clean
    }
    return "PNMB-${clean.replace(" ", "-")}"
}
