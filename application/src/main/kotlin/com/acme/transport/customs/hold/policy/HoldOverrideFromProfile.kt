package com.acme.transport.customs.hold.policy

import com.acme.transport.customs.tariff.profile.TariffProfile
import com.acme.transport.customs.tariff.profile.classifyTariffRule

fun summarizeTariffProfilesForOverride(items: MutableList<TariffProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveHoldOverridesFromProfile(items: MutableList<TariffProfile>): MutableList<HoldOverride> {
    val out = mutableListOf<HoldOverride>()
    for (item in items) {
        val derived = newHoldOverride(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignHoldOverride(item: HoldOverride): String = classifyTariffRule(item.measuredValue())
