package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.linehaul.tender.summary.TenderRequest
import com.acme.transport.linehaul.tender.summary.classifyTenderRequest

fun summarizeTenderRequestsForSlot(items: MutableList<TenderRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveLegacyLanePlanSlotsFromRequest(items: MutableList<TenderRequest>): MutableList<LegacyLanePlanSlot> {
    val out = mutableListOf<LegacyLanePlanSlot>()
    for (item in items) {
        val derived = newLegacyLanePlanSlot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeLegacyLanePlanSlot(item: LegacyLanePlanSlot): String = classifyTenderRequest(item.adjustedTotal())
