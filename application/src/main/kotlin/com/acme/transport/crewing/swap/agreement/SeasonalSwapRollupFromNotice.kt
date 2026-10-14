package com.acme.transport.crewing.swap.agreement

import com.acme.transport.crewing.swap.swapnotice.LegacySwapNotice
import com.acme.transport.crewing.swap.swapnotice.normalizeSwapTemplateCode

fun summarizeLegacySwapNoticesForRollup(items: MutableList<LegacySwapNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveSeasonalSwapRollupsFromNotice(items: MutableList<LegacySwapNotice>): MutableList<SeasonalSwapRollup> {
    val out = mutableListOf<SeasonalSwapRollup>()
    for (item in items) {
        val derived = newSeasonalSwapRollup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateSeasonalSwapRollupExtra(item: SeasonalSwapRollup): String = normalizeSwapTemplateCode(item.code)
