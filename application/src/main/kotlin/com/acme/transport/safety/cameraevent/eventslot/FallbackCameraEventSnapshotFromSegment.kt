package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.safety.cameraevent.eventbatch.CameraEventSegment
import com.acme.transport.safety.cameraevent.eventbatch.cameraEventSegmentBucket

fun summarizeCameraEventSegmentsForSnapshot(items: MutableList<CameraEventSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveFallbackCameraEventSnapshotsFromSegment(items: MutableList<CameraEventSegment>): MutableList<FallbackCameraEventSnapshot> {
    val out = mutableListOf<FallbackCameraEventSnapshot>()
    for (item in items) {
        val derived = newFallbackCameraEventSnapshot(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveFallbackCameraEventSnapshot(item: FallbackCameraEventSnapshot): String {
    return cameraEventSegmentBucket(item.weightedExposure())
}
