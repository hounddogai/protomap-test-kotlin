package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.linehaul.tender.proposal.ManualTenderPreference
import com.acme.transport.linehaul.tender.proposal.batchedTenderEstimateWeight

fun summarizeManualTenderPreferencesForTarget(items: MutableList<ManualTenderPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveManualLanePlanTargetsFromPreference(items: MutableList<ManualTenderPreference>): MutableList<ManualLanePlanTarget> {
    val out = mutableListOf<ManualLanePlanTarget>()
    for (item in items) {
        val derived = newManualLanePlanTarget(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceManualLanePlanTarget(item: ManualLanePlanTarget): Double = batchedTenderEstimateWeight(2, true)
