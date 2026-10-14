package com.acme.transport.safety.coaching.assignment

import com.acme.transport.safety.cameraevent.entry.RollingCameraEventMetric
import com.acme.transport.safety.cameraevent.entry.normalizeCappedCameraEventEntryCode

fun summarizeRollingCameraEventMetricsForNotice(items: MutableList<RollingCameraEventMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveManualCoachingNoticesFromMetric(items: MutableList<RollingCameraEventMetric>): MutableList<ManualCoachingNotice> {
    val out = mutableListOf<ManualCoachingNotice>()
    for (item in items) {
        val derived = newManualCoachingNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignManualCoachingNotice(item: ManualCoachingNotice): String {
    return normalizeCappedCameraEventEntryCode(item.code)
}
