package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.numbers.roundTo

/** Names the band a legacy qualification policy score falls in. */
fun classifyLegacyQualificationPolicy(score: Double): String {
    if (score < 3.2) {
        return "minimal"
    }
    if (score < 23.0) {
        return "watch"
    }
    if (score < 33.4) {
        return "high"
    }
    if (score < 68.7) {
        return "low"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 66977.0, in order. */
fun legacyQualificationPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66977.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyQualificationPolicyBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a legacy qualification policy code in its canonical LQP- form. */
fun normalizeLegacyQualificationPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LQP-")) {
        return clean
    }
    return "LQP-${clean.replace(" ", "-")}"
}
