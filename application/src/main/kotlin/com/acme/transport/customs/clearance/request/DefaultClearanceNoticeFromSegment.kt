package com.acme.transport.customs.clearance.request

import com.acme.transport.customs.declaration.rule.DeclarationSegment
import com.acme.transport.customs.declaration.rule.globalDeclarationAuditWeight

fun summarizeDeclarationSegmentsForNotice(items: MutableList<DeclarationSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDefaultClearanceNoticesFromSegment(items: MutableList<DeclarationSegment>): MutableList<DefaultClearanceNotice> {
    val out = mutableListOf<DefaultClearanceNotice>()
    for (item in items) {
        val derived = newDefaultClearanceNotice(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateDefaultClearanceNotice(item: DefaultClearanceNotice): Double = globalDeclarationAuditWeight(4, true)
