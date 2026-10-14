package com.acme.transport.emissions.idling.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIdlingForecast(): Int {
    val policy = defaultPolicy("idling-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 62270.0, in order. */
fun idlingForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62270.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun idlingForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a idling forecast code in its canonical IF- form. */
fun normalizeIdlingForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IF-")) {
        return clean
    }
    return "IF-${clean.replace(" ", "-")}"
}
