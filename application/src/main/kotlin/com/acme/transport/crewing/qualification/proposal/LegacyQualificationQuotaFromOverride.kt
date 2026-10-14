package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.crewing.restbreak.overrideitem.WeightedRestBreakOverride
import com.acme.transport.crewing.restbreak.overrideitem.normalizeLegacyRestBreakEstimateCode

fun summarizeWeightedRestBreakOverridesForQuota(items: MutableList<WeightedRestBreakOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLegacyQualificationQuotasFromOverride(items: MutableList<WeightedRestBreakOverride>): MutableList<LegacyQualificationQuota> {
    val out = mutableListOf<LegacyQualificationQuota>()
    for (item in items) {
        val derived = newLegacyQualificationQuota(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignLegacyQualificationQuota(item: LegacyQualificationQuota): String {
    return normalizeLegacyRestBreakEstimateCode(item.code)
}
