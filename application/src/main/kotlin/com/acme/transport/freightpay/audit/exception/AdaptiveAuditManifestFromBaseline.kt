package com.acme.transport.freightpay.audit.exception

import com.acme.transport.freightpay.accessorial.baseline.ManualAccessorialBaseline
import com.acme.transport.freightpay.accessorial.baseline.classifyTieredAccessorialBatch

fun summarizeManualAccessorialBaselinesForManifest(items: MutableList<ManualAccessorialBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveAdaptiveAuditManifestsFromBaseline(items: MutableList<ManualAccessorialBaseline>): MutableList<AdaptiveAuditManifest> {
    val out = mutableListOf<AdaptiveAuditManifest>()
    for (item in items) {
        val derived = newAdaptiveAuditManifest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectAdaptiveAuditManifest(item: AdaptiveAuditManifest): String {
    return classifyTieredAccessorialBatch(item.effectiveTotal())
}
