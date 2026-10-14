package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.terminal.dwell.review.DwellDraft
import com.acme.transport.terminal.dwell.review.normalizeGlobalDwellProposalCode

fun summarizeDwellDraftsForPlan(items: MutableList<DwellDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveRollingDwellPlansFromDraft(items: MutableList<DwellDraft>): MutableList<RollingDwellPlan> {
    val out = mutableListOf<RollingDwellPlan>()
    for (item in items) {
        val derived = newRollingDwellPlan(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRollingDwellPlanExtra(item: RollingDwellPlan): String = normalizeGlobalDwellProposalCode(item.code)
