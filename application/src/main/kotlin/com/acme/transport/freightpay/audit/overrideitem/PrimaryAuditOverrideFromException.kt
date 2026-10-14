package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.freightpay.fuelsurcharge.exception.FuelSurchargeException
import com.acme.transport.freightpay.fuelsurcharge.exception.normalizeFuelSurchargeSegmentCode

fun summarizeFuelSurchargeExceptionsForOverride(items: MutableList<FuelSurchargeException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryAuditOverridesFromException(items: MutableList<FuelSurchargeException>): MutableList<PrimaryAuditOverride> {
    val out = mutableListOf<PrimaryAuditOverride>()
    for (item in items) {
        val derived = newPrimaryAuditOverride(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computePrimaryAuditOverride(item: PrimaryAuditOverride): String = normalizeFuelSurchargeSegmentCode(item.code)
