package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun weightedAccessorialTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 89461.0, in order. */
fun weightedAccessorialTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 89461.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a weighted accessorial template code in its canonical WAT- form. */
fun normalizeWeightedAccessorialTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WAT-")) {
        return clean
    }
    return "WAT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedAccessorialTemplate(): Int {
    val policy = defaultPolicy("weighted-accessorial-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
