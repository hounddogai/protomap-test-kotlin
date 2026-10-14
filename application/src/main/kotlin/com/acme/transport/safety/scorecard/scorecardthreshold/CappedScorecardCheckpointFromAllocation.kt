package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.safety.cameraevent.defaulteventscore.TieredCameraEventAllocation
import com.acme.transport.safety.cameraevent.defaulteventscore.normalizeAdaptiveCameraEventGroupCode

fun summarizeTieredCameraEventAllocationsForCheckpoint(items: MutableList<TieredCameraEventAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveCappedScorecardCheckpointsFromAllocation(items: MutableList<TieredCameraEventAllocation>): MutableList<CappedScorecardCheckpoint> {
    val out = mutableListOf<CappedScorecardCheckpoint>()
    for (item in items) {
        val derived = newCappedScorecardCheckpoint(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCappedScorecardCheckpoint(item: CappedScorecardCheckpoint): String {
    return normalizeAdaptiveCameraEventGroupCode(item.code)
}
