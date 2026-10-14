package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.emissions.emissionfactor.summary.ScheduledEmissionFactorMetric
import com.acme.transport.emissions.emissionfactor.summary.emissionFactorGroupWeight

fun summarizeScheduledEmissionFactorMetricsForSlot(items: MutableList<ScheduledEmissionFactorMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefaultEvChargeSlotsFromMetric(items: MutableList<ScheduledEmissionFactorMetric>): MutableList<DefaultEvChargeSlot> {
    val out = mutableListOf<DefaultEvChargeSlot>()
    for (item in items) {
        val derived = newDefaultEvChargeSlot(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyDefaultEvChargeSlot(item: DefaultEvChargeSlot): Double = emissionFactorGroupWeight(8, true)
