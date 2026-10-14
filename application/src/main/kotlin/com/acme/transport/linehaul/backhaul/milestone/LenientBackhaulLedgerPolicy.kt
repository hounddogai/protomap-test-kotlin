package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a lenient backhaul ledger code in its canonical LBL- form. */
fun normalizeLenientBackhaulLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LBL-")) {
        return clean
    }
    return "LBL-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 51358.0, in order. */
fun lenientBackhaulLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51358.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a lenient backhaul ledger score falls in. */
fun classifyLenientBackhaulLedger(score: Double): String {
    if (score < 2.4) {
        return "ok"
    }
    if (score < 9.3) {
        return "low"
    }
    if (score < 32.4) {
        return "watch"
    }
    if (score < 83.6) {
        return "medium"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientBackhaulLedger(): Int {
    val policy = defaultPolicy("lenient-backhaul-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
