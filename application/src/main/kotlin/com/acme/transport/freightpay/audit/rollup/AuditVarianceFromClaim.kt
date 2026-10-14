package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.freightpay.audit.auditclaim.RollingAuditClaim
import com.acme.transport.freightpay.audit.auditclaim.globalAuditMilestoneWeight

fun summarizeRollingAuditClaimsForVariance(items: MutableList<RollingAuditClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveAuditVariancesFromClaim(items: MutableList<RollingAuditClaim>): MutableList<AuditVariance> {
    val out = mutableListOf<AuditVariance>()
    for (item in items) {
        val derived = newAuditVariance(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun balanceAuditVariance(item: AuditVariance): Double = globalAuditMilestoneWeight(6, true)
