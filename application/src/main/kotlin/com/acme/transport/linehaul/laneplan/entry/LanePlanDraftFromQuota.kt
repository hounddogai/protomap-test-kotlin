package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.linehaul.capacity.quota.CapacityQuota
import com.acme.transport.linehaul.capacity.quota.classifyCapacityPreference

fun summarizeCapacityQuotasForDraft(items: MutableList<CapacityQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveLanePlanDraftsFromQuota(items: MutableList<CapacityQuota>): MutableList<LanePlanDraft> {
    val out = mutableListOf<LanePlanDraft>()
    for (item in items) {
        val derived = newLanePlanDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceLanePlanDraft(item: LanePlanDraft): String = classifyCapacityPreference(item.projectedLoad())
