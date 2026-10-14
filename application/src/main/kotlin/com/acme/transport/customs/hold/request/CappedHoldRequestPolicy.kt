package com.acme.transport.customs.hold.request

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 42719.0, in order. */
fun cappedHoldRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 42719.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedHoldRequest(): Int {
    val policy = defaultPolicy("capped-hold-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped hold request code in its canonical CHR- form. */
fun normalizeCappedHoldRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CHR-")) {
        return clean
    }
    return "CHR-${clean.replace(" ", "-")}"
}
