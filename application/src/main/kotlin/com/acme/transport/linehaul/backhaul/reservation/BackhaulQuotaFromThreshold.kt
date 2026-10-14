package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.linehaul.laneplan.threshold.LanePlanThreshold
import com.acme.transport.linehaul.laneplan.threshold.lanePlanThresholdWeight

fun summarizeLanePlanThresholdsForQuota(items: MutableList<LanePlanThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveBackhaulQuotasFromThreshold(items: MutableList<LanePlanThreshold>): MutableList<BackhaulQuota> {
    val out = mutableListOf<BackhaulQuota>()
    for (item in items) {
        val derived = newBackhaulQuota(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planBackhaulQuota(item: BackhaulQuota): Double = lanePlanThresholdWeight(1, true)
