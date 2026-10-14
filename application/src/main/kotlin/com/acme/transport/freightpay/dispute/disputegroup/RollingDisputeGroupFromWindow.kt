package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.freightpay.fuelsurcharge.surchargewindow.ScheduledFuelSurchargeWindow
import com.acme.transport.freightpay.fuelsurcharge.surchargewindow.normalizeFuelSurchargeTierCode

fun summarizeScheduledFuelSurchargeWindowsForGroup(items: MutableList<ScheduledFuelSurchargeWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRollingDisputeGroupsFromWindow(items: MutableList<ScheduledFuelSurchargeWindow>): MutableList<RollingDisputeGroup> {
    val out = mutableListOf<RollingDisputeGroup>()
    for (item in items) {
        val derived = newRollingDisputeGroup(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceRollingDisputeGroup(item: RollingDisputeGroup): String = normalizeFuelSurchargeTierCode(item.code)
