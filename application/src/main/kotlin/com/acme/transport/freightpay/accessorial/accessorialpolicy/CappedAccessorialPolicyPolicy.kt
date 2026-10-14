package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedAccessorialPolicy(): Int {
    val policy = defaultPolicy("capped-accessorial-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped accessorial policy code in its canonical CAP- form. */
fun normalizeCappedAccessorialPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CAP-")) {
        return clean
    }
    return "CAP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 94228.0, in order. */
fun cappedAccessorialPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 94228.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
