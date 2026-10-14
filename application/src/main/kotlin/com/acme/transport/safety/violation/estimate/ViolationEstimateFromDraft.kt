package com.acme.transport.safety.violation.estimate

import com.acme.transport.safety.scorecard.profile.ScorecardDraft
import com.acme.transport.safety.scorecard.profile.normalizeScorecardDraftCode

fun summarizeScorecardDraftsForEstimate(items: MutableList<ScorecardDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveViolationEstimatesFromDraft(items: MutableList<ScorecardDraft>): MutableList<ViolationEstimate> {
    val out = mutableListOf<ViolationEstimate>()
    for (item in items) {
        val derived = newViolationEstimate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreViolationEstimate(item: ViolationEstimate): String = normalizeScorecardDraftCode(item.code)
