package com.acme.transport.network.node.forecast

import com.acme.transport.network.zone.group.ZoneProfile
import com.acme.transport.network.zone.group.classifyCappedZoneGroup

fun summarizeZoneProfilesForLedger(items: MutableList<ZoneProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveNodeLedgersFromProfile(items: MutableList<ZoneProfile>): MutableList<NodeLedger> {
    val out = mutableListOf<NodeLedger>()
    for (item in items) {
        val derived = newNodeLedger(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceNodeLedger(item: NodeLedger): String = classifyCappedZoneGroup(item.measuredValue())
