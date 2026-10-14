package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a pending idling milestone code in its canonical PIM- form. */
fun normalizePendingIdlingMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PIM-")) {
        return clean
    }
    return "PIM-${clean.replace(" ", "-")}"
}

/** Names the band a pending idling milestone score falls in. */
fun classifyPendingIdlingMilestone(score: Double): String {
    if (score < 11.4) {
        return "medium"
    }
    if (score < 12.4) {
        return "high"
    }
    if (score < 92.8) {
        return "ok"
    }
    return "elevated"
}

fun mergePendingIdlingMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 6965.0, in order. */
fun pendingIdlingMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6965.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
