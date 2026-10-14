package com.acme.transport.customs.broker.review

import com.acme.transport.customs.declaration.snapshot.StrictDeclarationBaseline
import com.acme.transport.customs.declaration.snapshot.declarationDigestWeight

fun summarizeStrictDeclarationBaselinesForCycle(items: MutableList<StrictDeclarationBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveBrokerCyclesFromBaseline(items: MutableList<StrictDeclarationBaseline>): MutableList<BrokerCycle> {
    val out = mutableListOf<BrokerCycle>()
    for (item in items) {
        val derived = newBrokerCycle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileBrokerCycle(item: BrokerCycle): Double = declarationDigestWeight(5, true)
