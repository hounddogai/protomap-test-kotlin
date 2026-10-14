package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.linehaul.backhaul.target.CappedBackhaulPolicy
import com.acme.transport.linehaul.backhaul.target.classifyBackhaulException

fun summarizeCappedBackhaulPoliciesForPreference(items: MutableList<CappedBackhaulPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRegionalTrailerPreferencesFromPolicy(items: MutableList<CappedBackhaulPolicy>): MutableList<RegionalTrailerPreference> {
    val out = mutableListOf<RegionalTrailerPreference>()
    for (item in items) {
        val derived = newRegionalTrailerPreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyRegionalTrailerPreference(item: RegionalTrailerPreference): String {
    return classifyBackhaulException(item.netValue())
}
