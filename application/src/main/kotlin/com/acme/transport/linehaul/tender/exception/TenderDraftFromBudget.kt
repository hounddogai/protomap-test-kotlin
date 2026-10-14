package com.acme.transport.linehaul.tender.exception

import com.acme.transport.linehaul.laneplan.budget.LanePlanBudget
import com.acme.transport.linehaul.laneplan.budget.normalizeLenientLanePlanPreferenceCode

fun summarizeLanePlanBudgetsForDraft(items: MutableList<LanePlanBudget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveTenderDraftsFromBudget(items: MutableList<LanePlanBudget>): MutableList<TenderDraft> {
    val out = mutableListOf<TenderDraft>()
    for (item in items) {
        val derived = newTenderDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateTenderDraft(item: TenderDraft): String = normalizeLenientLanePlanPreferenceCode(item.code)
