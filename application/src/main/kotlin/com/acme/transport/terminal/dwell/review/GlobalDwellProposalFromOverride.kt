package com.acme.transport.terminal.dwell.review

import com.acme.transport.terminal.traileryard.review.TrailerYardOverride
import com.acme.transport.terminal.traileryard.review.normalizeTrailerYardThresholdCode

fun summarizeTrailerYardOverridesForProposal(items: MutableList<TrailerYardOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveGlobalDwellProposalsFromOverride(items: MutableList<TrailerYardOverride>): MutableList<GlobalDwellProposal> {
    val out = mutableListOf<GlobalDwellProposal>()
    for (item in items) {
        val derived = newGlobalDwellProposal(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun scoreGlobalDwellProposal(item: GlobalDwellProposal): String = normalizeTrailerYardThresholdCode(item.code)
