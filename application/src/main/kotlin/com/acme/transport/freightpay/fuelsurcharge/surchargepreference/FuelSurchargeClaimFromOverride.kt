package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.freightpay.audit.overrideitem.PrimaryAuditOverride
import com.acme.transport.freightpay.audit.overrideitem.normalizeRollingAuditPolicyCode

fun summarizePrimaryAuditOverridesForClaim(items: MutableList<PrimaryAuditOverride>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveFuelSurchargeClaimsFromOverride(items: MutableList<PrimaryAuditOverride>): MutableList<FuelSurchargeClaim> {
    val out = mutableListOf<FuelSurchargeClaim>()
    for (item in items) {
        val derived = newFuelSurchargeClaim(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankFuelSurchargeClaim(item: FuelSurchargeClaim): String = normalizeRollingAuditPolicyCode(item.code)
