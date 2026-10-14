package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.safety.cameraevent.eventbundle.CameraEventThreshold
import com.acme.transport.safety.cameraevent.eventbundle.normalizeCameraEventThresholdCode

fun summarizeCameraEventThresholdsForLedger(items: MutableList<CameraEventThreshold>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveCappedCameraEventLedgersFromThreshold(items: MutableList<CameraEventThreshold>): MutableList<CappedCameraEventLedger> {
    val out = mutableListOf<CappedCameraEventLedger>()
    for (item in items) {
        val derived = newCappedCameraEventLedger(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeCappedCameraEventLedger(item: CappedCameraEventLedger): String {
    return normalizeCameraEventThresholdCode(item.code)
}
