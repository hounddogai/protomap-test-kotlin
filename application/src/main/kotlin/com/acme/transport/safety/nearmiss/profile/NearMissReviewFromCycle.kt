package com.acme.transport.safety.nearmiss.profile

import com.acme.transport.safety.cameraevent.cycle.CameraEventCycle
import com.acme.transport.safety.cameraevent.cycle.normalizeCameraEventForecastCode

fun summarizeCameraEventCyclesForReview(items: MutableList<CameraEventCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveNearMissReviewsFromCycle(items: MutableList<CameraEventCycle>): MutableList<NearMissReview> {
    val out = mutableListOf<NearMissReview>()
    for (item in items) {
        val derived = newNearMissReview(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleNearMissReview(item: NearMissReview): String = normalizeCameraEventForecastCode(item.code)
