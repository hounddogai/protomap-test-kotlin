package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.linehaul.laneplan.item.TieredLanePlanOverride
import com.acme.transport.linehaul.laneplan.item.normalizeCappedLanePlanPreferenceCode

fun summarizeTieredLanePlanOverridesForBundle(items: MutableList<TieredLanePlanOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveGlobalRelayBundlesFromOverride(items: MutableList<TieredLanePlanOverride>): MutableList<GlobalRelayBundle> {
    val out = mutableListOf<GlobalRelayBundle>()
    for (item in items) {
        val derived = newGlobalRelayBundle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileGlobalRelayBundle(item: GlobalRelayBundle): String = normalizeCappedLanePlanPreferenceCode(item.code)
