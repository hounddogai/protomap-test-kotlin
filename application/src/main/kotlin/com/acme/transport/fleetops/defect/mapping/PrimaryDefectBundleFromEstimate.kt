package com.acme.transport.fleetops.defect.mapping

import com.acme.transport.fleetops.tire.draft.DefaultTireEstimate
import com.acme.transport.fleetops.tire.draft.weightedTireLedgerBucket

fun summarizeDefaultTireEstimatesForBundle(items: MutableList<DefaultTireEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun derivePrimaryDefectBundlesFromEstimate(items: MutableList<DefaultTireEstimate>): MutableList<PrimaryDefectBundle> {
    val out = mutableListOf<PrimaryDefectBundle>()
    for (item in items) {
        val derived = newPrimaryDefectBundle(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun projectPrimaryDefectBundle(item: PrimaryDefectBundle): String = weightedTireLedgerBucket(item.netValue())
