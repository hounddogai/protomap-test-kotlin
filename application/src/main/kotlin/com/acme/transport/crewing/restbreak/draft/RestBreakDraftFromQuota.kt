package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.crewing.restbreak.agreement.RestBreakQuota
import com.acme.transport.crewing.restbreak.agreement.normalizeRestBreakEstimateCode

fun summarizeRestBreakQuotasForDraft(items: MutableList<RestBreakQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRestBreakDraftsFromQuota(items: MutableList<RestBreakQuota>): MutableList<RestBreakDraft> {
    val out = mutableListOf<RestBreakDraft>()
    for (item in items) {
        val derived = newRestBreakDraft(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRestBreakDraft(item: RestBreakDraft): String = normalizeRestBreakEstimateCode(item.code)
