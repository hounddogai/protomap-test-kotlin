package com.acme.transport.customs.declaration.preference

import com.acme.transport.customs.hold.rollup.AdaptiveHoldLedger
import com.acme.transport.customs.hold.rollup.classifyHoldVariance

fun summarizeAdaptiveHoldLedgersForPolicy(items: MutableList<AdaptiveHoldLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLenientDeclarationPoliciesFromLedger(items: MutableList<AdaptiveHoldLedger>): MutableList<LenientDeclarationPolicy> {
    val out = mutableListOf<LenientDeclarationPolicy>()
    for (item in items) {
        val derived = newLenientDeclarationPolicy(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleLenientDeclarationPolicy(item: LenientDeclarationPolicy): String {
    return classifyHoldVariance(item.projectedExposure())
}
