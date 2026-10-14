package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.linehaul.laneplan.planrule.LenientLanePlanRule
import com.acme.transport.linehaul.laneplan.planrule.normalizeLanePlanScoreCode

fun summarizeLenientLanePlanRulesForSlot(items: MutableList<LenientLanePlanRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveFallbackTenderSlotsFromRule(items: MutableList<LenientLanePlanRule>): MutableList<FallbackTenderSlot> {
    val out = mutableListOf<FallbackTenderSlot>()
    for (item in items) {
        val derived = newFallbackTenderSlot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun deriveFallbackTenderSlot(item: FallbackTenderSlot): String = normalizeLanePlanScoreCode(item.code)
