package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePrimaryCameraEventMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 76516.0, in order. */
fun primaryCameraEventMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76516.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a primary camera event milestone score falls in. */
fun classifyPrimaryCameraEventMilestone(score: Double): String {
    if (score < 22.3) {
        return "minimal"
    }
    if (score < 58.1) {
        return "medium"
    }
    if (score < 59.3) {
        return "ok"
    }
    return "high"
}

/** Puts a primary camera event milestone code in its canonical PCEM- form. */
fun normalizePrimaryCameraEventMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCEM-")) {
        return clean
    }
    return "PCEM-${clean.replace(" ", "-")}"
}
