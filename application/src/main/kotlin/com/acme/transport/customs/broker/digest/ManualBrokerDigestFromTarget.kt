package com.acme.transport.customs.broker.digest

import com.acme.transport.customs.clearance.target.ClearanceTarget
import com.acme.transport.customs.clearance.target.classifyPrimaryClearanceGroup

fun summarizeClearanceTargetsForDigest(items: MutableList<ClearanceTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveManualBrokerDigestsFromTarget(items: MutableList<ClearanceTarget>): MutableList<ManualBrokerDigest> {
    val out = mutableListOf<ManualBrokerDigest>()
    for (item in items) {
        val derived = newManualBrokerDigest(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeManualBrokerDigest(item: ManualBrokerDigest): String {
    return classifyPrimaryClearanceGroup(item.measuredValue())
}
