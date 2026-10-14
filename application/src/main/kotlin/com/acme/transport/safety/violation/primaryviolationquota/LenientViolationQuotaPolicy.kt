package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientViolationQuota(): Int {
    val policy = defaultPolicy("lenient-violation-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a lenient violation quota code in its canonical LVQ- form. */
fun normalizeLenientViolationQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LVQ-")) {
        return clean
    }
    return "LVQ-${clean.replace(" ", "-")}"
}

fun lenientViolationQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 47509.0, in order. */
fun lenientViolationQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47509.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
