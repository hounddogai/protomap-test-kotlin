package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 12560.0, in order. */
fun cameraEventProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12560.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a camera event proposal score falls in. */
fun classifyCameraEventProposal(score: Double): String {
    if (score < 14.7) {
        return "ok"
    }
    if (score < 49.2) {
        return "minimal"
    }
    if (score < 76.7) {
        return "elevated"
    }
    return "watch"
}

/** Puts a camera event proposal code in its canonical CEP- form. */
fun normalizeCameraEventProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CEP-")) {
        return clean
    }
    return "CEP-${clean.replace(" ", "-")}"
}

fun mergeCameraEventProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
