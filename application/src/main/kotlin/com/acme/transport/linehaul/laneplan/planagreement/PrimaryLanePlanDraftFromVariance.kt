package com.acme.transport.linehaul.laneplan.planagreement

import com.acme.transport.linehaul.capacity.capacityassignment.PrimaryCapacityVariance
import com.acme.transport.linehaul.capacity.capacityassignment.capacityAdjustmentWeight

fun summarizePrimaryCapacityVariancesForDraft(items: MutableList<PrimaryCapacityVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun derivePrimaryLanePlanDraftsFromVariance(items: MutableList<PrimaryCapacityVariance>): MutableList<PrimaryLanePlanDraft> {
    val out = mutableListOf<PrimaryLanePlanDraft>()
    for (item in items) {
        val derived = newPrimaryLanePlanDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computePrimaryLanePlanDraft(item: PrimaryLanePlanDraft): Double = capacityAdjustmentWeight(4, true)
