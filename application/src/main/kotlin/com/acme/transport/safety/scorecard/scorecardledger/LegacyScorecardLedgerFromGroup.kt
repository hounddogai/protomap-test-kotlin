package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.safety.cameraevent.ledger.CameraEventGroup
import com.acme.transport.safety.cameraevent.ledger.normalizeCameraEventEstimateCode

fun summarizeCameraEventGroupsForLedger(items: MutableList<CameraEventGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyScorecardLedgersFromGroup(items: MutableList<CameraEventGroup>): MutableList<LegacyScorecardLedger> {
    val out = mutableListOf<LegacyScorecardLedger>()
    for (item in items) {
        val derived = newLegacyScorecardLedger(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLegacyScorecardLedger(item: LegacyScorecardLedger): String = normalizeCameraEventEstimateCode(item.code)
