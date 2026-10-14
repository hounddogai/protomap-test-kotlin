package com.acme.transport.customs.clearance.manifest

import com.acme.transport.customs.hold.policy.HoldPolicy
import com.acme.transport.customs.hold.policy.holdOverrideBucket

fun summarizeHoldPoliciesForManifest(items: MutableList<HoldPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveClearanceManifestsFromPolicy(items: MutableList<HoldPolicy>): MutableList<ClearanceManifest> {
    val out = mutableListOf<ClearanceManifest>()
    for (item in items) {
        val derived = newClearanceManifest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreClearanceManifest(item: ClearanceManifest): String = holdOverrideBucket(item.adjustedValue())
