package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.safety.scorecard.ledger.ScorecardRecord
import com.acme.transport.safety.scorecard.ledger.classifyScorecardAllocation

fun summarizeScorecardRecordsForMilestone(items: MutableList<ScorecardRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCameraEventMilestonesFromRecord(items: MutableList<ScorecardRecord>): MutableList<CameraEventMilestone> {
    val out = mutableListOf<CameraEventMilestone>()
    for (item in items) {
        val derived = newCameraEventMilestone(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun rankCameraEventMilestone(item: CameraEventMilestone): String {
    return classifyScorecardAllocation(item.measuredValue())
}
