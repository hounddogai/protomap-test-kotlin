package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.safety.scorecard.slot.ScorecardAssignment
import com.acme.transport.safety.scorecard.slot.tieredScorecardNoticeBucket

fun summarizeScorecardAssignmentsForNotice(items: MutableList<ScorecardAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveCameraEventNoticesFromAssignment(items: MutableList<ScorecardAssignment>): MutableList<CameraEventNotice> {
    val out = mutableListOf<CameraEventNotice>()
    for (item in items) {
        val derived = newCameraEventNotice(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchCameraEventNotice(item: CameraEventNotice): String = tieredScorecardNoticeBucket(item.adjustedTotal())
