package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.freightpay.audit.exception.StrictAuditSchedule
import com.acme.transport.freightpay.audit.exception.auditExceptionBucket

fun summarizeStrictAuditSchedulesForPlan(items: MutableList<StrictAuditSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveFuelSurchargePlansFromSchedule(items: MutableList<StrictAuditSchedule>): MutableList<FuelSurchargePlan> {
    val out = mutableListOf<FuelSurchargePlan>()
    for (item in items) {
        val derived = newFuelSurchargePlan(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitFuelSurchargePlan(item: FuelSurchargePlan): String = auditExceptionBucket(item.adjustedExposure())
