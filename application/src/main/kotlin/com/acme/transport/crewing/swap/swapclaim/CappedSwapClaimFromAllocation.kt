package com.acme.transport.crewing.swap.swapclaim

import com.acme.transport.crewing.qualification.allocation.RollingQualificationAllocation
import com.acme.transport.crewing.qualification.allocation.classifyDefaultQualificationSummary

fun summarizeRollingQualificationAllocationsForClaim(items: MutableList<RollingQualificationAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveCappedSwapClaimsFromAllocation(items: MutableList<RollingQualificationAllocation>): MutableList<CappedSwapClaim> {
    val out = mutableListOf<CappedSwapClaim>()
    for (item in items) {
        val derived = newCappedSwapClaim(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyCappedSwapClaim(item: CappedSwapClaim): String = classifyDefaultQualificationSummary(item.netTotal())
