package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.lastmile.redelivery.slot.ManualRedeliveryWindow
import com.acme.transport.lastmile.redelivery.slot.strictRedeliveryForecastBucket

fun summarizeManualRedeliveryWindowsForManifest(items: MutableList<ManualRedeliveryWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveAdaptiveAttemptManifestsFromWindow(items: MutableList<ManualRedeliveryWindow>): MutableList<AdaptiveAttemptManifest> {
    val out = mutableListOf<AdaptiveAttemptManifest>()
    for (item in items) {
        val derived = newAdaptiveAttemptManifest(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveAdaptiveAttemptManifest(item: AdaptiveAttemptManifest): String {
    return strictRedeliveryForecastBucket(item.netExposure())
}
