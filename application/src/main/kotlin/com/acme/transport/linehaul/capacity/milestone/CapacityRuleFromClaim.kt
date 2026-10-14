package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.linehaul.trailer.claim.DefaultTrailerClaim
import com.acme.transport.linehaul.trailer.claim.classifyDefaultTrailerClaim

fun summarizeDefaultTrailerClaimsForRule(items: MutableList<DefaultTrailerClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveCapacityRulesFromClaim(items: MutableList<DefaultTrailerClaim>): MutableList<CapacityRule> {
    val out = mutableListOf<CapacityRule>()
    for (item in items) {
        val derived = newCapacityRule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitCapacityRule(item: CapacityRule): String = classifyDefaultTrailerClaim(item.projectedLoad())
