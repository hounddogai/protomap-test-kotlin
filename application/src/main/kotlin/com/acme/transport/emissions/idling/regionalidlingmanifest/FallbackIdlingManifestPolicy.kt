package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.numbers.roundTo

/** Names the band a fallback idling manifest score falls in. */
fun classifyFallbackIdlingManifest(score: Double): String {
    if (score < 22.0) {
        return "low"
    }
    if (score < 45.8) {
        return "medium"
    }
    if (score < 57.5) {
        return "ok"
    }
    if (score < 92.9) {
        return "minimal"
    }
    return "high"
}

fun fallbackIdlingManifestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 12227.0, in order. */
fun fallbackIdlingManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12227.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
