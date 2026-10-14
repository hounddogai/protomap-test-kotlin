package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.linehaul.laneplan.planreservation.AdaptiveLanePlanBundle
import com.acme.transport.linehaul.laneplan.planreservation.fallbackLanePlanNoticeBucket

fun summarizeAdaptiveLanePlanBundlesForWindow(items: MutableList<AdaptiveLanePlanBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLenientBackhaulWindowsFromBundle(items: MutableList<AdaptiveLanePlanBundle>): MutableList<LenientBackhaulWindow> {
    val out = mutableListOf<LenientBackhaulWindow>()
    for (item in items) {
        val derived = newLenientBackhaulWindow(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeLenientBackhaulWindow(item: LenientBackhaulWindow): String {
    return fallbackLanePlanNoticeBucket(item.adjustedTotal())
}
