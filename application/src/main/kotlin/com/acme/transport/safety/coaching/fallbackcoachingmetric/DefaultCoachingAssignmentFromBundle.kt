package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.safety.cameraevent.eventaudit.ManualCameraEventBundle
import com.acme.transport.safety.cameraevent.eventaudit.lenientCameraEventMetricBucket

fun summarizeManualCameraEventBundlesForAssignment(items: MutableList<ManualCameraEventBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultCoachingAssignmentsFromBundle(items: MutableList<ManualCameraEventBundle>): MutableList<DefaultCoachingAssignment> {
    val out = mutableListOf<DefaultCoachingAssignment>()
    for (item in items) {
        val derived = newDefaultCoachingAssignment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankDefaultCoachingAssignment(item: DefaultCoachingAssignment): String {
    return lenientCameraEventMetricBucket(item.effectiveVolume())
}
