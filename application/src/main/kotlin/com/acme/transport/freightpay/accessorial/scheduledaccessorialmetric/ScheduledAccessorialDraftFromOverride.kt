package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.freightpay.accessorial.overrideitem.PrimaryAccessorialOverride
import com.acme.transport.freightpay.accessorial.overrideitem.manualAccessorialMetricBucket

fun summarizePrimaryAccessorialOverridesForDraft(items: MutableList<PrimaryAccessorialOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveScheduledAccessorialDraftsFromOverride(items: MutableList<PrimaryAccessorialOverride>): MutableList<ScheduledAccessorialDraft> {
    val out = mutableListOf<ScheduledAccessorialDraft>()
    for (item in items) {
        val derived = newScheduledAccessorialDraft(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun planScheduledAccessorialDraft(item: ScheduledAccessorialDraft): String {
    return manualAccessorialMetricBucket(item.adjustedValue())
}
