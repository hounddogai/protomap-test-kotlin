package com.acme.transport.customs.tariff.tariffscore

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a default tariff target code in its canonical DTT- form. */
fun normalizeDefaultTariffTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DTT-")) {
        return clean
    }
    return "DTT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 98236.0, in order. */
fun defaultTariffTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98236.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultTariffTarget(): Int {
    val policy = defaultPolicy("default-tariff-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a default tariff target score falls in. */
fun classifyDefaultTariffTarget(score: Double): String {
    if (score < 5.1) {
        return "ok"
    }
    if (score < 43.5) {
        return "elevated"
    }
    if (score < 63.6) {
        return "high"
    }
    if (score < 66.8) {
        return "minimal"
    }
    return "watch"
}
