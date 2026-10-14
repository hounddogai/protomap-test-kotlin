package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.safety.nearmiss.notice.RollingNearMissSegment
import com.acme.transport.safety.nearmiss.notice.normalizeRollingNearMissSegmentCode

fun summarizeRollingNearMissSegmentsForAllocation(items: MutableList<RollingNearMissSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveAdaptiveCameraEventAllocationsFromSegment(items: MutableList<RollingNearMissSegment>): MutableList<AdaptiveCameraEventAllocation> {
    val out = mutableListOf<AdaptiveCameraEventAllocation>()
    for (item in items) {
        val derived = newAdaptiveCameraEventAllocation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyAdaptiveCameraEventAllocation(item: AdaptiveCameraEventAllocation): String {
    return normalizeRollingNearMissSegmentCode(item.code)
}
