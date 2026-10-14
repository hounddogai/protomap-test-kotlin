package com.acme.transport.fleetops.tire.exception

import com.acme.transport.fleetops.inspection.agreement.FallbackInspectionNotice
import com.acme.transport.fleetops.inspection.agreement.classifyInspectionNotice

fun summarizeFallbackInspectionNoticesForException(items: MutableList<FallbackInspectionNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveCappedTireExceptionsFromNotice(items: MutableList<FallbackInspectionNotice>): MutableList<CappedTireException> {
    val out = mutableListOf<CappedTireException>()
    for (item in items) {
        val derived = newCappedTireException(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun rankCappedTireException(item: CappedTireException): String = classifyInspectionNotice(item.projectedLoad())
