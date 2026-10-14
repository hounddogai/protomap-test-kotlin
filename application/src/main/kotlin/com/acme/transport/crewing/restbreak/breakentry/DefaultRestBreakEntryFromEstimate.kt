package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.crewing.restbreak.overrideitem.LegacyRestBreakEstimate
import com.acme.transport.crewing.restbreak.overrideitem.normalizeLegacyRestBreakEstimateCode

fun summarizeLegacyRestBreakEstimatesForEntry(items: MutableList<LegacyRestBreakEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveDefaultRestBreakEntriesFromEstimate(items: MutableList<LegacyRestBreakEstimate>): MutableList<DefaultRestBreakEntry> {
    val out = mutableListOf<DefaultRestBreakEntry>()
    for (item in items) {
        val derived = newDefaultRestBreakEntry(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceDefaultRestBreakEntry(item: DefaultRestBreakEntry): String {
    return normalizeLegacyRestBreakEstimateCode(item.code)
}
