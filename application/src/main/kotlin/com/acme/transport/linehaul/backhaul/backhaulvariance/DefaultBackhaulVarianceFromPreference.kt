package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.linehaul.tender.batch.TenderPreference
import com.acme.transport.linehaul.tender.batch.classifyTenderPreference

fun summarizeTenderPreferencesForVariance(items: MutableList<TenderPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDefaultBackhaulVariancesFromPreference(items: MutableList<TenderPreference>): MutableList<DefaultBackhaulVariance> {
    val out = mutableListOf<DefaultBackhaulVariance>()
    for (item in items) {
        val derived = newDefaultBackhaulVariance(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeDefaultBackhaulVariance(item: DefaultBackhaulVariance): String {
    return classifyTenderPreference(item.measuredValue())
}
