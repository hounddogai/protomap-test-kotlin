package com.acme.transport.linehaul.relay.audit

import com.acme.transport.linehaul.tender.recorditem.LenientTenderCheckpoint
import com.acme.transport.linehaul.tender.recorditem.classifyLenientTenderCheckpoint

fun summarizeLenientTenderCheckpointsForMilestone(items: MutableList<LenientTenderCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveSeasonalRelayMilestonesFromCheckpoint(items: MutableList<LenientTenderCheckpoint>): MutableList<SeasonalRelayMilestone> {
    val out = mutableListOf<SeasonalRelayMilestone>()
    for (item in items) {
        val derived = newSeasonalRelayMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankSeasonalRelayMilestone(item: SeasonalRelayMilestone): String {
    return classifyLenientTenderCheckpoint(item.measuredValue())
}
