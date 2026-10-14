package com.acme.transport.network.transittime.timenotice

import com.acme.transport.network.servicearea.manualareathreshold.ServiceAreaPreference
import com.acme.transport.network.servicearea.manualareathreshold.normalizeStrictServiceAreaBundleCode

fun summarizeServiceAreaPreferencesForNotice(items: MutableList<ServiceAreaPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun derivePrimaryTransitTimeNoticesFromPreference(items: MutableList<ServiceAreaPreference>): MutableList<PrimaryTransitTimeNotice> {
    val out = mutableListOf<PrimaryTransitTimeNotice>()
    for (item in items) {
        val derived = newPrimaryTransitTimeNotice(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizePrimaryTransitTimeNotice(item: PrimaryTransitTimeNotice): String {
    return normalizeStrictServiceAreaBundleCode(item.code)
}
