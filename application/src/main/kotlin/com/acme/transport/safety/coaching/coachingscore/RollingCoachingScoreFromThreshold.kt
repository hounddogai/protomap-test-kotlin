package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.safety.cameraevent.eventbundle.CameraEventThreshold
import com.acme.transport.safety.cameraevent.eventbundle.pendingCameraEventBundleWeight

fun summarizeCameraEventThresholdsForScore(items: MutableList<CameraEventThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveRollingCoachingScoresFromThreshold(items: MutableList<CameraEventThreshold>): MutableList<RollingCoachingScore> {
    val out = mutableListOf<RollingCoachingScore>()
    for (item in items) {
        val derived = newRollingCoachingScore(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planRollingCoachingScore(item: RollingCoachingScore): Double = pendingCameraEventBundleWeight(6, true)
