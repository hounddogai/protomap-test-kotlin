package com.acme.transport.safety.scorecard.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeScorecardBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a scorecard budget score falls in. */
fun classifyScorecardBudget(score: Double): String {
    if (score < 43.6) {
        return "ok"
    }
    if (score < 44.8) {
        return "high"
    }
    if (score < 61.3) {
        return "elevated"
    }
    if (score < 70.1) {
        return "minimal"
    }
    return "watch"
}

/** Puts a scorecard budget code in its canonical SB- form. */
fun normalizeScorecardBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SB-")) {
        return clean
    }
    return "SB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScorecardBudget(): Int {
    val policy = defaultPolicy("scorecard-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
