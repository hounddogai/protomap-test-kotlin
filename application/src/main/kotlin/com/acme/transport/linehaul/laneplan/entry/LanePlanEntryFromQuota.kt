package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.linehaul.tender.profile.TenderQuota
import com.acme.transport.linehaul.tender.profile.classifyTenderTemplate

fun summarizeTenderQuotasForEntry(items: MutableList<TenderQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLanePlanEntriesFromQuota(items: MutableList<TenderQuota>): MutableList<LanePlanEntry> {
    val out = mutableListOf<LanePlanEntry>()
    for (item in items) {
        val derived = newLanePlanEntry(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyLanePlanEntry(item: LanePlanEntry): String = classifyTenderTemplate(item.projectedTotal())
