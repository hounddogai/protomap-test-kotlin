package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.safety.nearmiss.metric.LenientNearMissMapping
import com.acme.transport.safety.nearmiss.metric.classifyLenientNearMissMapping

fun summarizeLenientNearMissMappingsForDraft(items: MutableList<LenientNearMissMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveCameraEventDraftsFromMapping(items: MutableList<LenientNearMissMapping>): MutableList<CameraEventDraft> {
    val out = mutableListOf<CameraEventDraft>()
    for (item in items) {
        val derived = newCameraEventDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyCameraEventDraft(item: CameraEventDraft): String {
    return classifyLenientNearMissMapping(item.effectiveVolume())
}
