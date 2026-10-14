package com.acme.transport.freightpay.fuelsurcharge.baseline

import com.acme.transport.freightpay.audit.cycle.FallbackAuditTarget
import com.acme.transport.freightpay.audit.cycle.auditThresholdBucket

fun summarizeFallbackAuditTargetsForLedger(items: MutableList<FallbackAuditTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRollingFuelSurchargeLedgersFromTarget(items: MutableList<FallbackAuditTarget>): MutableList<RollingFuelSurchargeLedger> {
    val out = mutableListOf<RollingFuelSurchargeLedger>()
    for (item in items) {
        val derived = newRollingFuelSurchargeLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRollingFuelSurchargeLedger(item: RollingFuelSurchargeLedger): String {
    return auditThresholdBucket(item.measuredValue())
}
