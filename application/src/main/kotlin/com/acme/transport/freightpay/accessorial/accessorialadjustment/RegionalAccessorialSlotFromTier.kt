package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.freightpay.fuelsurcharge.surchargewindow.FuelSurchargeTier
import com.acme.transport.freightpay.fuelsurcharge.surchargewindow.normalizeFuelSurchargeTierCode

fun summarizeFuelSurchargeTiersForSlot(items: MutableList<FuelSurchargeTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveRegionalAccessorialSlotsFromTier(items: MutableList<FuelSurchargeTier>): MutableList<RegionalAccessorialSlot> {
    val out = mutableListOf<RegionalAccessorialSlot>()
    for (item in items) {
        val derived = newRegionalAccessorialSlot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateRegionalAccessorialSlot(item: RegionalAccessorialSlot): String {
    return normalizeFuelSurchargeTierCode(item.code)
}
