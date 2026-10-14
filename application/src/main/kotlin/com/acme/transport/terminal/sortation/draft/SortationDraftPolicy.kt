package com.acme.transport.terminal.sortation.draft

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a sortation draft code in its canonical SD- form. */
fun normalizeSortationDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SD-")) {
        return clean
    }
    return "SD-${clean.replace(" ", "-")}"
}

fun sortationDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a sortation draft score falls in. */
fun classifySortationDraft(score: Double): String {
    if (score < 63.5) {
        return "minimal"
    }
    if (score < 75.5) {
        return "elevated"
    }
    if (score < 97.5) {
        return "high"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSortationDraft(): Int {
    val policy = defaultPolicy("sortation-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
