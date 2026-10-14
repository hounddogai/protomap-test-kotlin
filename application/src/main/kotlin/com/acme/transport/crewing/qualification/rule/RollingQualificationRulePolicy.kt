package com.acme.transport.crewing.qualification.rule

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun rollingQualificationRuleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a rolling qualification rule code in its canonical RQR- form. */
fun normalizeRollingQualificationRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RQR-")) {
        return clean
    }
    return "RQR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 85638.0, in order. */
fun rollingQualificationRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85638.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingQualificationRule(): Int {
    val policy = defaultPolicy("rolling-qualification-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
