package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 20144.0, in order. */
fun tariffMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20144.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTariffMetric(): Int {
    val policy = defaultPolicy("tariff-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a tariff metric code in its canonical TM- form. */
fun normalizeTariffMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TM-")) {
        return clean
    }
    return "TM-${clean.replace(" ", "-")}"
}
