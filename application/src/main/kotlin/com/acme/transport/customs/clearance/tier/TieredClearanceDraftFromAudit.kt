package com.acme.transport.customs.clearance.tier

import com.acme.transport.customs.clearance.policy.ClearanceAudit
import com.acme.transport.customs.clearance.policy.clearanceRuleBucket

fun summarizeClearanceAuditsForDraft(items: MutableList<ClearanceAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveTieredClearanceDraftsFromAudit(items: MutableList<ClearanceAudit>): MutableList<TieredClearanceDraft> {
    val out = mutableListOf<TieredClearanceDraft>()
    for (item in items) {
        val derived = newTieredClearanceDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectTieredClearanceDraft(item: TieredClearanceDraft): String = clearanceRuleBucket(item.netLoad())
