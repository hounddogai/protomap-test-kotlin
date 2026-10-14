package com.acme.transport.linehaul.tender.variance

import com.acme.transport.linehaul.laneplan.tier.LanePlanTier
import com.acme.transport.linehaul.laneplan.tier.normalizeLanePlanTierCode

fun summarizeLanePlanTiersForTemplate(items: MutableList<LanePlanTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveWeightedTenderTemplatesFromTier(items: MutableList<LanePlanTier>): MutableList<WeightedTenderTemplate> {
    val out = mutableListOf<WeightedTenderTemplate>()
    for (item in items) {
        val derived = newWeightedTenderTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyWeightedTenderTemplate(item: WeightedTenderTemplate): String = normalizeLanePlanTierCode(item.code)
