package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.lastmile.attempt.allocation.AttemptAllocation
import com.acme.transport.lastmile.attempt.allocation.normalizeAttemptAdjustmentCode

fun summarizeAttemptAllocationsForDraft(items: MutableList<AttemptAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveAttemptDraftsFromAllocation(items: MutableList<AttemptAllocation>): MutableList<AttemptDraft> {
    val out = mutableListOf<AttemptDraft>()
    for (item in items) {
        val derived = newAttemptDraft(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun projectAttemptDraft(item: AttemptDraft): String = normalizeAttemptAdjustmentCode(item.code)
