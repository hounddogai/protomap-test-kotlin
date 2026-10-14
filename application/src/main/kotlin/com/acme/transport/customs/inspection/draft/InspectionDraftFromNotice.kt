package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.clearance.notice.SeasonalClearanceNotice
import com.acme.transport.customs.clearance.notice.normalizeSeasonalClearanceNoticeCode

fun summarizeSeasonalClearanceNoticesForDraft(items: MutableList<SeasonalClearanceNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveInspectionDraftsFromNotice(items: MutableList<SeasonalClearanceNotice>): MutableList<InspectionDraft> {
    val out = mutableListOf<InspectionDraft>()
    for (item in items) {
        val derived = newInspectionDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planInspectionDraft(item: InspectionDraft): String = normalizeSeasonalClearanceNoticeCode(item.code)
