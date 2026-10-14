package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 61137.0, in order. */
fun lenientZoneAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61137.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientZoneAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientZoneAgreement(): Int {
    val policy = defaultPolicy("lenient-zone-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a lenient zone agreement code in its canonical LZA- form. */
fun normalizeLenientZoneAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LZA-")) {
        return clean
    }
    return "LZA-${clean.replace(" ", "-")}"
}
