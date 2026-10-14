package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.freightpay.fuelsurcharge.baseline.RollingFuelSurchargeLedger
import com.acme.transport.freightpay.fuelsurcharge.baseline.classifyRollingFuelSurchargeLedger

fun summarizeRollingFuelSurchargeLedgersForClaim(items: MutableList<RollingFuelSurchargeLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveSeasonalFuelSurchargeClaimsFromLedger(items: MutableList<RollingFuelSurchargeLedger>): MutableList<SeasonalFuelSurchargeClaim> {
    val out = mutableListOf<SeasonalFuelSurchargeClaim>()
    for (item in items) {
        val derived = newSeasonalFuelSurchargeClaim(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateSeasonalFuelSurchargeClaim(item: SeasonalFuelSurchargeClaim): String {
    return classifyRollingFuelSurchargeLedger(item.measuredValue())
}
