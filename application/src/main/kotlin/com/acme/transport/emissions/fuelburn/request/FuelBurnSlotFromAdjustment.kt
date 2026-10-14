package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.emissions.carbonreport.adjustment.CarbonReportAdjustment
import com.acme.transport.emissions.carbonreport.adjustment.classifyWeightedCarbonReportLedger

fun summarizeCarbonReportAdjustmentsForSlot(items: MutableList<CarbonReportAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveFuelBurnSlotsFromAdjustment(items: MutableList<CarbonReportAdjustment>): MutableList<FuelBurnSlot> {
    val out = mutableListOf<FuelBurnSlot>()
    for (item in items) {
        val derived = newFuelBurnSlot(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankFuelBurnSlot(item: FuelBurnSlot): String = classifyWeightedCarbonReportLedger(item.measuredValue())
