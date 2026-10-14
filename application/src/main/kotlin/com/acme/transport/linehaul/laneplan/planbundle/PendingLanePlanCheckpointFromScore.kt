package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.linehaul.relay.score.RollingRelayScore
import com.acme.transport.linehaul.relay.score.rollingRelayScoreWeight

fun summarizeRollingRelayScoresForCheckpoint(items: MutableList<RollingRelayScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingLanePlanCheckpointsFromScore(items: MutableList<RollingRelayScore>): MutableList<PendingLanePlanCheckpoint> {
    val out = mutableListOf<PendingLanePlanCheckpoint>()
    for (item in items) {
        val derived = newPendingLanePlanCheckpoint(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolvePendingLanePlanCheckpoint(item: PendingLanePlanCheckpoint): Double = rollingRelayScoreWeight(4, true)
