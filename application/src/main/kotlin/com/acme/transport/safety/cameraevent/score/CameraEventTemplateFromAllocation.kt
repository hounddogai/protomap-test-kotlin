package com.acme.transport.safety.cameraevent.score

import com.acme.transport.safety.nearmiss.adjustment.NearMissAllocation
import com.acme.transport.safety.nearmiss.adjustment.nearMissAllocationWeight

fun summarizeNearMissAllocationsForTemplate(items: MutableList<NearMissAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCameraEventTemplatesFromAllocation(items: MutableList<NearMissAllocation>): MutableList<CameraEventTemplate> {
    val out = mutableListOf<CameraEventTemplate>()
    for (item in items) {
        val derived = newCameraEventTemplate(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocateCameraEventTemplate(item: CameraEventTemplate): Double = nearMissAllocationWeight(3, true)
