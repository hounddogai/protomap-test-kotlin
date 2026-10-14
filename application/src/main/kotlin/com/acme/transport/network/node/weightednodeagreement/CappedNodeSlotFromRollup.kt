package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.network.servicearea.rollup.ServiceAreaRollup
import com.acme.transport.network.servicearea.rollup.classifyServiceAreaTemplate

fun summarizeServiceAreaRollupsForSlot(items: MutableList<ServiceAreaRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedNodeSlotsFromRollup(items: MutableList<ServiceAreaRollup>): MutableList<CappedNodeSlot> {
    val out = mutableListOf<CappedNodeSlot>()
    for (item in items) {
        val derived = newCappedNodeSlot(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun computeCappedNodeSlotExtra(item: CappedNodeSlot): String {
    return classifyServiceAreaTemplate(item.adjustedExposure())
}
