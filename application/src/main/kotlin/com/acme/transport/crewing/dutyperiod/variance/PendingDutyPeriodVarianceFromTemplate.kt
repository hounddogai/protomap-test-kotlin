package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.crewing.restbreak.breakmapping.WeightedRestBreakTemplate
import com.acme.transport.crewing.restbreak.breakmapping.defaultRestBreakVarianceWeight

fun summarizeWeightedRestBreakTemplatesForVariance(items: MutableList<WeightedRestBreakTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun derivePendingDutyPeriodVariancesFromTemplate(items: MutableList<WeightedRestBreakTemplate>): MutableList<PendingDutyPeriodVariance> {
    val out = mutableListOf<PendingDutyPeriodVariance>()
    for (item in items) {
        val derived = newPendingDutyPeriodVariance(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computePendingDutyPeriodVariance(item: PendingDutyPeriodVariance): Double {
    return defaultRestBreakVarianceWeight(4, true)
}
