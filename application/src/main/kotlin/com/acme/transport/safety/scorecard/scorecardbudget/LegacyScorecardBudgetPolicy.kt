package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyScorecardBudget(): Int {
    val policy = defaultPolicy("legacy-scorecard-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a legacy scorecard budget code in its canonical LSB- form. */
fun normalizeLegacyScorecardBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSB-")) {
        return clean
    }
    return "LSB-${clean.replace(" ", "-")}"
}

fun mergeLegacyScorecardBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy scorecard budget score falls in. */
fun classifyLegacyScorecardBudget(score: Double): String {
    if (score < 17.8) {
        return "ok"
    }
    if (score < 20.0) {
        return "watch"
    }
    if (score < 99.4) {
        return "minimal"
    }
    return "elevated"
}
