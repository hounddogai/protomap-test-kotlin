package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun accessorialSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAccessorialSnapshot(): Int {
    val policy = defaultPolicy("accessorial-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a accessorial snapshot code in its canonical AS- form. */
fun normalizeAccessorialSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AS-")) {
        return clean
    }
    return "AS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 54978.0, in order. */
fun accessorialSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54978.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
