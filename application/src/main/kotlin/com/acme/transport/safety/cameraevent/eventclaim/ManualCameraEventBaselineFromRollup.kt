package com.acme.transport.safety.cameraevent.eventclaim

import com.acme.transport.safety.scorecard.scorecardthreshold.BatchedScorecardRollup
import com.acme.transport.safety.scorecard.scorecardthreshold.primaryScorecardAdjustmentWeight

fun summarizeBatchedScorecardRollupsForBaseline(items: MutableList<BatchedScorecardRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveManualCameraEventBaselinesFromRollup(items: MutableList<BatchedScorecardRollup>): MutableList<ManualCameraEventBaseline> {
    val out = mutableListOf<ManualCameraEventBaseline>()
    for (item in items) {
        val derived = newManualCameraEventBaseline(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreManualCameraEventBaseline(item: ManualCameraEventBaseline): Double {
    return primaryScorecardAdjustmentWeight(9, true)
}
