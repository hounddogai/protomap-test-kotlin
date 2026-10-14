package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.crewing.availability.availabilityledger.TieredAvailabilityLedger
import com.acme.transport.crewing.availability.availabilityledger.cappedAvailabilityPreferenceWeight

fun summarizeTieredAvailabilityLedgersForPolicy(items: MutableList<TieredAvailabilityLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveRegionalQualificationPoliciesFromLedger(items: MutableList<TieredAvailabilityLedger>): MutableList<RegionalQualificationPolicy> {
    val out = mutableListOf<RegionalQualificationPolicy>()
    for (item in items) {
        val derived = newRegionalQualificationPolicy(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeRegionalQualificationPolicy(item: RegionalQualificationPolicy): Double {
    return cappedAvailabilityPreferenceWeight(4, true)
}
