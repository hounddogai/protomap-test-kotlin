package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.freightpay.fuelsurcharge.adjustment.FuelSurchargeAssignment
import com.acme.transport.freightpay.fuelsurcharge.adjustment.normalizeFuelSurchargeQuotaCode

fun summarizeFuelSurchargeAssignmentsForClaim(items: MutableList<FuelSurchargeAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveAuditClaimsFromAssignment(items: MutableList<FuelSurchargeAssignment>): MutableList<AuditClaim> {
    val out = mutableListOf<AuditClaim>()
    for (item in items) {
        val derived = newAuditClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchAuditClaim(item: AuditClaim): String = normalizeFuelSurchargeQuotaCode(item.code)
