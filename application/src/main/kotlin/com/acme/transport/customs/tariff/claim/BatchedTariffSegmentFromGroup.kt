package com.acme.transport.customs.tariff.claim

import com.acme.transport.customs.declaration.assignment.TieredDeclarationGroup
import com.acme.transport.customs.declaration.assignment.declarationPolicyBucket

fun summarizeTieredDeclarationGroupsForSegment(items: MutableList<TieredDeclarationGroup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBatchedTariffSegmentsFromGroup(items: MutableList<TieredDeclarationGroup>): MutableList<BatchedTariffSegment> {
    val out = mutableListOf<BatchedTariffSegment>()
    for (item in items) {
        val derived = newBatchedTariffSegment(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planBatchedTariffSegment(item: BatchedTariffSegment): String = declarationPolicyBucket(item.measuredValue())
