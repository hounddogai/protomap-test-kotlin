package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.crewing.qualification.preference.PendingQualificationMetric
import com.acme.transport.crewing.qualification.preference.normalizePendingQualificationMetricCode

fun summarizePendingQualificationMetricsForSnapshot(items: MutableList<PendingQualificationMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveLegacyRestBreakSnapshotsFromMetric(items: MutableList<PendingQualificationMetric>): MutableList<LegacyRestBreakSnapshot> {
    val out = mutableListOf<LegacyRestBreakSnapshot>()
    for (item in items) {
        val derived = newLegacyRestBreakSnapshot(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveLegacyRestBreakSnapshot(item: LegacyRestBreakSnapshot): String {
    return normalizePendingQualificationMetricCode(item.code)
}
