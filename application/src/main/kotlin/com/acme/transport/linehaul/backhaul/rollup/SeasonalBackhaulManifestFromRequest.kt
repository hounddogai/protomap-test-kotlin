package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.linehaul.tender.summary.TenderRequest
import com.acme.transport.linehaul.tender.summary.normalizeTenderSummaryCode

fun summarizeTenderRequestsForManifest(items: MutableList<TenderRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveSeasonalBackhaulManifestsFromRequest(items: MutableList<TenderRequest>): MutableList<SeasonalBackhaulManifest> {
    val out = mutableListOf<SeasonalBackhaulManifest>()
    for (item in items) {
        val derived = newSeasonalBackhaulManifest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveSeasonalBackhaulManifest(item: SeasonalBackhaulManifest): String {
    return normalizeTenderSummaryCode(item.code)
}
