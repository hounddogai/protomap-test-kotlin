package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.emissions.carbonreport.policy.CarbonReportPolicy
import com.acme.transport.emissions.carbonreport.policy.normalizeCarbonReportPolicyCode

fun summarizeCarbonReportPoliciesForSnapshot(items: MutableList<CarbonReportPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveFuelBurnSnapshotsFromPolicy(items: MutableList<CarbonReportPolicy>): MutableList<FuelBurnSnapshot> {
    val out = mutableListOf<FuelBurnSnapshot>()
    for (item in items) {
        val derived = newFuelBurnSnapshot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileFuelBurnSnapshot(item: FuelBurnSnapshot): String = normalizeCarbonReportPolicyCode(item.code)
