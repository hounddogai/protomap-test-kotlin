package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.freightpay.dispute.disputebundle.GlobalDisputeBatch
import com.acme.transport.freightpay.dispute.disputebundle.normalizeDisputeManifestCode

fun summarizeGlobalDisputeBatchesForPreference(items: MutableList<GlobalDisputeBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveAccessorialPreferencesFromBatch(items: MutableList<GlobalDisputeBatch>): MutableList<AccessorialPreference> {
    val out = mutableListOf<AccessorialPreference>()
    for (item in items) {
        val derived = newAccessorialPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeAccessorialPreference(item: AccessorialPreference): String = normalizeDisputeManifestCode(item.code)
