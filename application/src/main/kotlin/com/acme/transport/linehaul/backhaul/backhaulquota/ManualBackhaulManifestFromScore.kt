package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.linehaul.tender.metric.PendingTenderScore
import com.acme.transport.linehaul.tender.metric.normalizeDefaultTenderMetricCode

fun summarizePendingTenderScoresForManifest(items: MutableList<PendingTenderScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveManualBackhaulManifestsFromScore(items: MutableList<PendingTenderScore>): MutableList<ManualBackhaulManifest> {
    val out = mutableListOf<ManualBackhaulManifest>()
    for (item in items) {
        val derived = newManualBackhaulManifest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeManualBackhaulManifest(item: ManualBackhaulManifest): String {
    return normalizeDefaultTenderMetricCode(item.code)
}
