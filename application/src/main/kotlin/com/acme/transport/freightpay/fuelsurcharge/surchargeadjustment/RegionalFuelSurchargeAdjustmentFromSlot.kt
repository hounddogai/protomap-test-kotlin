package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.freightpay.audit.notice.RegionalAuditSlot
import com.acme.transport.freightpay.audit.notice.auditNoticeWeight

fun summarizeRegionalAuditSlotsForAdjustment(items: MutableList<RegionalAuditSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveRegionalFuelSurchargeAdjustmentsFromSlot(items: MutableList<RegionalAuditSlot>): MutableList<RegionalFuelSurchargeAdjustment> {
    val out = mutableListOf<RegionalFuelSurchargeAdjustment>()
    for (item in items) {
        val derived = newRegionalFuelSurchargeAdjustment(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleRegionalFuelSurchargeAdjustment(item: RegionalFuelSurchargeAdjustment): Double {
    return auditNoticeWeight(8, true)
}
