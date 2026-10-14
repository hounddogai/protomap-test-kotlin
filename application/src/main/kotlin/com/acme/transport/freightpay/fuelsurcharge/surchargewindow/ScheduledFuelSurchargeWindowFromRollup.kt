package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import com.acme.transport.freightpay.dispute.disputeagreement.AdaptiveDisputeRollup
import com.acme.transport.freightpay.dispute.disputeagreement.adaptiveDisputeRollupWeight

fun summarizeAdaptiveDisputeRollupsForWindow(items: MutableList<AdaptiveDisputeRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveScheduledFuelSurchargeWindowsFromRollup(items: MutableList<AdaptiveDisputeRollup>): MutableList<ScheduledFuelSurchargeWindow> {
    val out = mutableListOf<ScheduledFuelSurchargeWindow>()
    for (item in items) {
        val derived = newScheduledFuelSurchargeWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceScheduledFuelSurchargeWindow(item: ScheduledFuelSurchargeWindow): Double {
    return adaptiveDisputeRollupWeight(9, true)
}
