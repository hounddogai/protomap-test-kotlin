package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedRestBreakOverride(): Int {
    val policy = defaultPolicy("weighted-rest-break-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a weighted rest break override score falls in. */
fun classifyWeightedRestBreakOverride(score: Double): String {
    if (score < 21.9) {
        return "ok"
    }
    if (score < 37.4) {
        return "high"
    }
    return "low"
}

/** Puts a weighted rest break override code in its canonical WRBO- form. */
fun normalizeWeightedRestBreakOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WRBO-")) {
        return clean
    }
    return "WRBO-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 83293.0, in order. */
fun weightedRestBreakOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83293.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
