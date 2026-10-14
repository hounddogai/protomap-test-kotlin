package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.freightpay.dispute.adjustment.DisputeClaim
import com.acme.transport.freightpay.dispute.adjustment.disputeAuditWeight

fun summarizeDisputeClaimsForPreference(items: MutableList<DisputeClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveFuelSurchargePreferencesFromClaim(items: MutableList<DisputeClaim>): MutableList<FuelSurchargePreference> {
    val out = mutableListOf<FuelSurchargePreference>()
    for (item in items) {
        val derived = newFuelSurchargePreference(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleFuelSurchargePreference(item: FuelSurchargePreference): Double = disputeAuditWeight(3, true)
