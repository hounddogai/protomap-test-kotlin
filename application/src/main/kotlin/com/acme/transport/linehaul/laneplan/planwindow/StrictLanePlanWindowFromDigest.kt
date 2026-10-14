package com.acme.transport.linehaul.laneplan.planwindow

import com.acme.transport.linehaul.backhaul.backhauldigest.FallbackBackhaulDigest
import com.acme.transport.linehaul.backhaul.backhauldigest.normalizeAdaptiveBackhaulQuotaCode

fun summarizeFallbackBackhaulDigestsForWindow(items: MutableList<FallbackBackhaulDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveStrictLanePlanWindowsFromDigest(items: MutableList<FallbackBackhaulDigest>): MutableList<StrictLanePlanWindow> {
    val out = mutableListOf<StrictLanePlanWindow>()
    for (item in items) {
        val derived = newStrictLanePlanWindow(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileStrictLanePlanWindow(item: StrictLanePlanWindow): String {
    return normalizeAdaptiveBackhaulQuotaCode(item.code)
}
