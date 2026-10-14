package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.freightpay.dispute.window.DisputeWindow
import com.acme.transport.freightpay.dispute.window.disputeTemplateBucket

fun summarizeDisputeWindowsForSlot(items: MutableList<DisputeWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveFuelSurchargeSlotsFromWindow(items: MutableList<DisputeWindow>): MutableList<FuelSurchargeSlot> {
    val out = mutableListOf<FuelSurchargeSlot>()
    for (item in items) {
        val derived = newFuelSurchargeSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateFuelSurchargeSlot(item: FuelSurchargeSlot): String = disputeTemplateBucket(item.weightedExposure())
