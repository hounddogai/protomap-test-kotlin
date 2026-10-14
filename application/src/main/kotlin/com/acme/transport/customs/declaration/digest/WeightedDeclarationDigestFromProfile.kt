package com.acme.transport.customs.declaration.digest

import com.acme.transport.customs.hold.holdmapping.ManualHoldProfile
import com.acme.transport.customs.hold.holdmapping.normalizeManualHoldProfileCode

fun summarizeManualHoldProfilesForDigest(items: MutableList<ManualHoldProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveWeightedDeclarationDigestsFromProfile(items: MutableList<ManualHoldProfile>): MutableList<WeightedDeclarationDigest> {
    val out = mutableListOf<WeightedDeclarationDigest>()
    for (item in items) {
        val derived = newWeightedDeclarationDigest(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileWeightedDeclarationDigest(item: WeightedDeclarationDigest): String {
    return normalizeManualHoldProfileCode(item.code)
}
