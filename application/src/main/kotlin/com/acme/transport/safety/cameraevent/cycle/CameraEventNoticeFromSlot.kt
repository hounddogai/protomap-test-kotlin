package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.safety.nearmiss.slot.NearMissSlot
import com.acme.transport.safety.nearmiss.slot.nearMissReservationBucket

fun summarizeNearMissSlotsForNotice(items: MutableList<NearMissSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveCameraEventNoticesFromSlot(items: MutableList<NearMissSlot>): MutableList<CameraEventNotice> {
    val out = mutableListOf<CameraEventNotice>()
    for (item in items) {
        val derived = newCameraEventNotice(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitCameraEventNotice(item: CameraEventNotice): String = nearMissReservationBucket(item.adjustedTotal())
