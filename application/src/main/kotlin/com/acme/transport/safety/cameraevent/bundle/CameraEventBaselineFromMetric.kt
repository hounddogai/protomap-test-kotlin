package com.acme.transport.safety.cameraevent.bundle

import com.acme.transport.safety.nearmiss.variance.DefaultNearMissMetric
import com.acme.transport.safety.nearmiss.variance.classifyBatchedNearMissSegment

fun summarizeDefaultNearMissMetricsForBaseline(items: MutableList<DefaultNearMissMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCameraEventBaselinesFromMetric(items: MutableList<DefaultNearMissMetric>): MutableList<CameraEventBaseline> {
    val out = mutableListOf<CameraEventBaseline>()
    for (item in items) {
        val derived = newCameraEventBaseline(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateCameraEventBaseline(item: CameraEventBaseline): String {
    return classifyBatchedNearMissSegment(item.effectiveExposure())
}
