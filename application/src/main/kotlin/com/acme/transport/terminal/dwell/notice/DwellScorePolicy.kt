package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun dwellScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.65
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDwellScore(): Int {
    val policy = defaultPolicy("dwell-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a dwell score code in its canonical DS- form. */
fun normalizeDwellScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DS-")) {
        return clean
    }
    return "DS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 12666.0, in order. */
fun dwellScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12666.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
