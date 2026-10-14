package com.acme.transport.terminal.traileryard.score

import com.acme.transport.terminal.traileryard.exception.TrailerYardPolicy
import com.acme.transport.terminal.traileryard.exception.trailerYardPolicyWeight

fun summarizeTrailerYardPoliciesForScore(items: MutableList<TrailerYardPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveTrailerYardScoresFromPolicy(items: MutableList<TrailerYardPolicy>): MutableList<TrailerYardScore> {
    val out = mutableListOf<TrailerYardScore>()
    for (item in items) {
        val derived = newTrailerYardScore(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planTrailerYardScore(item: TrailerYardScore): Double = trailerYardPolicyWeight(8, true)
