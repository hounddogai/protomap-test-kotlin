package com.acme.transport.linehaul.relay.relaytarget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalRelayTarget(): Int {
    val policy = defaultPolicy("seasonal-relay-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 32787.0, in order. */
fun seasonalRelayTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32787.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun seasonalRelayTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a seasonal relay target code in its canonical SRT- form. */
fun normalizeSeasonalRelayTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRT-")) {
        return clean
    }
    return "SRT-${clean.replace(" ", "-")}"
}
