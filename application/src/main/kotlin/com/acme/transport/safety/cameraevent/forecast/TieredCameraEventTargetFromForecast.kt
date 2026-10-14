package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.safety.scorecard.rule.PendingScorecardForecast
import com.acme.transport.safety.scorecard.rule.pendingScorecardForecastBucket

fun summarizePendingScorecardForecastsForTarget(items: MutableList<PendingScorecardForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveTieredCameraEventTargetsFromForecast(items: MutableList<PendingScorecardForecast>): MutableList<TieredCameraEventTarget> {
    val out = mutableListOf<TieredCameraEventTarget>()
    for (item in items) {
        val derived = newTieredCameraEventTarget(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchTieredCameraEventTarget(item: TieredCameraEventTarget): String {
    return pendingScorecardForecastBucket(item.netVolume())
}
