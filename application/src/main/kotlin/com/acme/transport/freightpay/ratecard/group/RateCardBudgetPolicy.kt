package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rate card budget code in its canonical RCB- form. */
fun normalizeRateCardBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCB-")) {
        return clean
    }
    return "RCB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardBudget(): Int {
    val policy = defaultPolicy("rate-card-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
