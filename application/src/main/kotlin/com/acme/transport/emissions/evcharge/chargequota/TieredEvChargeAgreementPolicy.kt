package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredEvChargeAgreement(): Int {
    val policy = defaultPolicy("tiered-ev-charge-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 78994.0, in order. */
fun tieredEvChargeAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78994.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered ev charge agreement code in its canonical TECA- form. */
fun normalizeTieredEvChargeAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TECA-")) {
        return clean
    }
    return "TECA-${clean.replace(" ", "-")}"
}

fun tieredEvChargeAgreementBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
