package com.acme.transport.terminal.traileryard.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveTrailerYardTarget(): Int {
    val policy = defaultPolicy("adaptive-trailer-yard-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a adaptive trailer yard target code in its canonical ATYT- form. */
fun normalizeAdaptiveTrailerYardTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ATYT-")) {
        return clean
    }
    return "ATYT-${clean.replace(" ", "-")}"
}

fun mergeAdaptiveTrailerYardTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 59559.0, in order. */
fun adaptiveTrailerYardTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 59559.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
