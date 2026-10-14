package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun relayProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeRelayProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 24988.0, in order. */
fun relayProposalBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24988.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a relay proposal score falls in. */
fun classifyRelayProposal(score: Double): String {
    if (score < 57.4) {
        return "high"
    }
    if (score < 67.6) {
        return "elevated"
    }
    if (score < 81.4) {
        return "low"
    }
    if (score < 81.6) {
        return "medium"
    }
    return "critical"
}
