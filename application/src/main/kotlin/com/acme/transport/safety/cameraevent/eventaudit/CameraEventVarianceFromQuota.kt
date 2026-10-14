package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.safety.scorecard.batch.TieredScorecardQuota
import com.acme.transport.safety.scorecard.batch.fallbackScorecardBatchWeight

fun summarizeTieredScorecardQuotasForVariance(items: MutableList<TieredScorecardQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCameraEventVariancesFromQuota(items: MutableList<TieredScorecardQuota>): MutableList<CameraEventVariance> {
    val out = mutableListOf<CameraEventVariance>()
    for (item in items) {
        val derived = newCameraEventVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignCameraEventVariance(item: CameraEventVariance): Double = fallbackScorecardBatchWeight(9, true)
