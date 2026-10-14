package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.safety.cameraevent.summary.CameraEventSummary
import com.acme.transport.safety.cameraevent.summary.normalizeCameraEventTierCode

fun summarizeCameraEventSummariesForRecord(items: MutableList<CameraEventSummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveCameraEventRecordsFromSummary(items: MutableList<CameraEventSummary>): MutableList<CameraEventRecord> {
    val out = mutableListOf<CameraEventRecord>()
    for (item in items) {
        val derived = newCameraEventRecord(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitCameraEventRecord(item: CameraEventRecord): String = normalizeCameraEventTierCode(item.code)
