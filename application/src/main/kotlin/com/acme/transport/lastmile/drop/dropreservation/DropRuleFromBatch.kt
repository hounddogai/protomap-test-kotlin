package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.lastmile.locker.batch.GlobalLockerBatch
import com.acme.transport.lastmile.locker.batch.globalLockerBatchWeight

fun summarizeGlobalLockerBatchesForRule(items: MutableList<GlobalLockerBatch>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDropRulesFromBatch(items: MutableList<GlobalLockerBatch>): MutableList<DropRule> {
    val out = mutableListOf<DropRule>()
    for (item in items) {
        val derived = newDropRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDropRule(item: DropRule): Double = globalLockerBatchWeight(4, true)
