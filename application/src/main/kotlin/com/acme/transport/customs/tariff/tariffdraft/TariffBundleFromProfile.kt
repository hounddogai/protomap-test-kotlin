package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.customs.hold.summary.HoldProfile
import com.acme.transport.customs.hold.summary.classifyHoldProfile

fun summarizeHoldProfilesForBundle(items: MutableList<HoldProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTariffBundlesFromProfile(items: MutableList<HoldProfile>): MutableList<TariffBundle> {
    val out = mutableListOf<TariffBundle>()
    for (item in items) {
        val derived = newTariffBundle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planTariffBundle(item: TariffBundle): String = classifyHoldProfile(item.netVolume())
