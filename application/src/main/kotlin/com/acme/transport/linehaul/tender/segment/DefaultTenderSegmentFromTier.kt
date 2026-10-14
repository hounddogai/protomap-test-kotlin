package com.acme.transport.linehaul.tender.segment

import com.acme.transport.linehaul.relay.relaythreshold.RollingRelayTier
import com.acme.transport.linehaul.relay.relaythreshold.classifyLegacyRelayRule

fun summarizeRollingRelayTiersForSegment(items: MutableList<RollingRelayTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDefaultTenderSegmentsFromTier(items: MutableList<RollingRelayTier>): MutableList<DefaultTenderSegment> {
    val out = mutableListOf<DefaultTenderSegment>()
    for (item in items) {
        val derived = newDefaultTenderSegment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignDefaultTenderSegment(item: DefaultTenderSegment): String = classifyLegacyRelayRule(item.measuredValue())
