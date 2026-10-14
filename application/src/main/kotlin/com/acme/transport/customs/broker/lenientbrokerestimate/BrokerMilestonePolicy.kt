package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 65153.0, in order. */
fun brokerMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 65153.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a broker milestone code in its canonical BM- form. */
fun normalizeBrokerMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BM-")) {
        return clean
    }
    return "BM-${clean.replace(" ", "-")}"
}

fun mergeBrokerMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a broker milestone score falls in. */
fun classifyBrokerMilestone(score: Double): String {
    if (score < 13.0) {
        return "ok"
    }
    if (score < 61.8) {
        return "minimal"
    }
    if (score < 82.2) {
        return "critical"
    }
    return "high"
}
