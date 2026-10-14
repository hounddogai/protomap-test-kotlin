package com.acme.transport.customs.declaration.batch

import com.acme.transport.customs.hold.policy.HoldClaim
import com.acme.transport.customs.hold.policy.classifyHoldClaim

fun summarizeHoldClaimsForBatch(items: MutableList<HoldClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDeclarationBatchesFromClaim(items: MutableList<HoldClaim>): MutableList<DeclarationBatch> {
    val out = mutableListOf<DeclarationBatch>()
    for (item in items) {
        val derived = newDeclarationBatch(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyDeclarationBatchExtra(item: DeclarationBatch): String = classifyHoldClaim(item.projectedExposure())
