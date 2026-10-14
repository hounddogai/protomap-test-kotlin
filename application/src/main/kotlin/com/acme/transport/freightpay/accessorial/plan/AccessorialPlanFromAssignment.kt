package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.freightpay.dispute.assignment.DisputeAssignment
import com.acme.transport.freightpay.dispute.assignment.disputeSummaryBucket

fun summarizeDisputeAssignmentsForPlan(items: MutableList<DisputeAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveAccessorialPlansFromAssignment(items: MutableList<DisputeAssignment>): MutableList<AccessorialPlan> {
    val out = mutableListOf<AccessorialPlan>()
    for (item in items) {
        val derived = newAccessorialPlan(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreAccessorialPlan(item: AccessorialPlan): String = disputeSummaryBucket(item.netLoad())
