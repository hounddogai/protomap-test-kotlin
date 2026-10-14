package com.acme.transport.linehaul.tender.entry

import com.acme.transport.linehaul.laneplan.planthreshold.RollingLanePlanDraft
import com.acme.transport.linehaul.laneplan.planthreshold.classifyRollingLanePlanDraft

fun summarizeRollingLanePlanDraftsForRecord(items: MutableList<RollingLanePlanDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveScheduledTenderRecordsFromDraft(items: MutableList<RollingLanePlanDraft>): MutableList<ScheduledTenderRecord> {
    val out = mutableListOf<ScheduledTenderRecord>()
    for (item in items) {
        val derived = newScheduledTenderRecord(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledTenderRecord(item: ScheduledTenderRecord): String {
    return classifyRollingLanePlanDraft(item.effectiveExposure())
}
