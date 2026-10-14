package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.network.cutoff.quota.CappedCutoffManifest
import com.acme.transport.network.cutoff.quota.classifyCappedCutoffManifest

fun summarizeCappedCutoffManifestsForTemplate(items: MutableList<CappedCutoffManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveCutoffTemplatesFromManifest(items: MutableList<CappedCutoffManifest>): MutableList<CutoffTemplate> {
    val out = mutableListOf<CutoffTemplate>()
    for (item in items) {
        val derived = newCutoffTemplate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun splitCutoffTemplate(item: CutoffTemplate): String = classifyCappedCutoffManifest(item.weightedExposure())
