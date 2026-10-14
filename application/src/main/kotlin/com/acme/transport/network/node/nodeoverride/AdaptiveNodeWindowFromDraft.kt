package com.acme.transport.network.node.nodeoverride

import com.acme.transport.network.cutoff.cutofftarget.ManualCutoffDraft
import com.acme.transport.network.cutoff.cutofftarget.regionalCutoffOverrideBucket

fun summarizeManualCutoffDraftsForWindow(items: MutableList<ManualCutoffDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveAdaptiveNodeWindowsFromDraft(items: MutableList<ManualCutoffDraft>): MutableList<AdaptiveNodeWindow> {
    val out = mutableListOf<AdaptiveNodeWindow>()
    for (item in items) {
        val derived = newAdaptiveNodeWindow(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeAdaptiveNodeWindow(item: AdaptiveNodeWindow): String {
    return regionalCutoffOverrideBucket(item.projectedVolume())
}
