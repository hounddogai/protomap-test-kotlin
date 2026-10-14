package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.safety.coaching.ledger.RollingCoachingPreference
import com.acme.transport.safety.coaching.ledger.classifyCoachingVariance

fun summarizeRollingCoachingPreferencesForTarget(items: MutableList<RollingCoachingPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCameraEventTargetsFromPreference(items: MutableList<RollingCoachingPreference>): MutableList<CameraEventTarget> {
    val out = mutableListOf<CameraEventTarget>()
    for (item in items) {
        val derived = newCameraEventTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeCameraEventTarget(item: CameraEventTarget): String = classifyCoachingVariance(item.netLoad())
