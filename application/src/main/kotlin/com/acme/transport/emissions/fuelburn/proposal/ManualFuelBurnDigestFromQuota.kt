package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.emissions.idling.idlingquota.ManualIdlingQuota
import com.acme.transport.emissions.idling.idlingquota.normalizeManualIdlingQuotaCode

fun summarizeManualIdlingQuotasForDigest(items: MutableList<ManualIdlingQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveManualFuelBurnDigestsFromQuota(items: MutableList<ManualIdlingQuota>): MutableList<ManualFuelBurnDigest> {
    val out = mutableListOf<ManualFuelBurnDigest>()
    for (item in items) {
        val derived = newManualFuelBurnDigest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun scoreManualFuelBurnDigest(item: ManualFuelBurnDigest): String = normalizeManualIdlingQuotaCode(item.code)
