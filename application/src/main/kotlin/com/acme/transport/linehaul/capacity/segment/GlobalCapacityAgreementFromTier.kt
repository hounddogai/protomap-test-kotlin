package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.linehaul.relay.bundle.PendingRelayTier
import com.acme.transport.linehaul.relay.bundle.classifyLenientRelaySummary

fun summarizePendingRelayTiersForAgreement(items: MutableList<PendingRelayTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveGlobalCapacityAgreementsFromTier(items: MutableList<PendingRelayTier>): MutableList<GlobalCapacityAgreement> {
    val out = mutableListOf<GlobalCapacityAgreement>()
    for (item in items) {
        val derived = newGlobalCapacityAgreement(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun rankGlobalCapacityAgreement(item: GlobalCapacityAgreement): String {
    return classifyLenientRelaySummary(item.measuredValue())
}
