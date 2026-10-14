package com.acme.transport.customs.inspection.batch

import com.acme.transport.customs.broker.allocation.BrokerAssignment
import com.acme.transport.customs.broker.allocation.brokerAssignmentBucket

fun summarizeBrokerAssignmentsForBundle(items: MutableList<BrokerAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveInspectionBundlesFromAssignment(items: MutableList<BrokerAssignment>): MutableList<InspectionBundle> {
    val out = mutableListOf<InspectionBundle>()
    for (item in items) {
        val derived = newInspectionBundle(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun planInspectionBundle(item: InspectionBundle): String = brokerAssignmentBucket(item.projectedExposure())
