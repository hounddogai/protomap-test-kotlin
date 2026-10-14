package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.safety.cameraevent.score.CameraEventScore
import com.acme.transport.safety.cameraevent.score.cameraEventTemplateBucket

fun summarizeCameraEventScoresForBudget(items: MutableList<CameraEventScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveNearMissBudgetsFromScore(items: MutableList<CameraEventScore>): MutableList<NearMissBudget> {
    val out = mutableListOf<NearMissBudget>()
    for (item in items) {
        val derived = newNearMissBudget(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun scoreNearMissBudget(item: NearMissBudget): String = cameraEventTemplateBucket(item.weightedVolume())
