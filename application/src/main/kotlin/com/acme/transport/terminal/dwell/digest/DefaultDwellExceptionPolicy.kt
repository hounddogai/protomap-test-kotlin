package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultDwellException(): Int {
    val policy = defaultPolicy("default-dwell-exception")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultDwellExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 5754.0, in order. */
fun defaultDwellExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5754.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a default dwell exception code in its canonical DDE- form. */
fun normalizeDefaultDwellExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDE-")) {
        return clean
    }
    return "DDE-${clean.replace(" ", "-")}"
}
