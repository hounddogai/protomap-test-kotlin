package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.linehaul.laneplan.item.LanePlanAdjustment
import com.acme.transport.linehaul.laneplan.item.normalizeLanePlanItemCode

fun summarizeLanePlanAdjustmentsForException(items: MutableList<LanePlanAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveDefaultCapacityExceptionsFromAdjustment(items: MutableList<LanePlanAdjustment>): MutableList<DefaultCapacityException> {
    val out = mutableListOf<DefaultCapacityException>()
    for (item in items) {
        val derived = newDefaultCapacityException(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDefaultCapacityException(item: DefaultCapacityException): String {
    return normalizeLanePlanItemCode(item.code)
}
