package com.acme.transport.safety.coaching.notice

import com.acme.transport.safety.cameraevent.score.CameraEventTemplate
import com.acme.transport.safety.cameraevent.score.classifyCameraEventScore

fun summarizeCameraEventTemplatesForNotice(items: MutableList<CameraEventTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveCappedCoachingNoticesFromTemplate(items: MutableList<CameraEventTemplate>): MutableList<CappedCoachingNotice> {
    val out = mutableListOf<CappedCoachingNotice>()
    for (item in items) {
        val derived = newCappedCoachingNotice(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectCappedCoachingNotice(item: CappedCoachingNotice): String {
    return classifyCameraEventScore(item.effectiveVolume())
}
