package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.linehaul.tender.variance.ManualTenderVariance
import com.acme.transport.linehaul.tender.variance.manualTenderVarianceWeight

fun summarizeManualTenderVariancesForAgreement(items: MutableList<ManualTenderVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveRegionalCapacityAgreementsFromVariance(items: MutableList<ManualTenderVariance>): MutableList<RegionalCapacityAgreement> {
    val out = mutableListOf<RegionalCapacityAgreement>()
    for (item in items) {
        val derived = newRegionalCapacityAgreement(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchRegionalCapacityAgreement(item: RegionalCapacityAgreement): Double = manualTenderVarianceWeight(8, true)
