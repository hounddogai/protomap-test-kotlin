package com.acme.transport.network.servicearea.window

import com.acme.transport.network.cutoff.quota.CappedCutoffManifest
import com.acme.transport.network.cutoff.quota.cutoffQuotaBucket

fun summarizeCappedCutoffManifestsForAudit(items: MutableList<CappedCutoffManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLenientServiceAreaAuditsFromManifest(items: MutableList<CappedCutoffManifest>): MutableList<LenientServiceAreaAudit> {
    val out = mutableListOf<LenientServiceAreaAudit>()
    for (item in items) {
        val derived = newLenientServiceAreaAudit(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignLenientServiceAreaAudit(item: LenientServiceAreaAudit): String {
    return cutoffQuotaBucket(item.weightedVolume())
}
