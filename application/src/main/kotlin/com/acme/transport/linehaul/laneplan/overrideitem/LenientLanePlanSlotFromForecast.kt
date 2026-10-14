package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.linehaul.tender.tenderforecast.BatchedTenderForecast
import com.acme.transport.linehaul.tender.tenderforecast.classifyCappedTenderClaim

fun summarizeBatchedTenderForecastsForSlot(items: MutableList<BatchedTenderForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveLenientLanePlanSlotsFromForecast(items: MutableList<BatchedTenderForecast>): MutableList<LenientLanePlanSlot> {
    val out = mutableListOf<LenientLanePlanSlot>()
    for (item in items) {
        val derived = newLenientLanePlanSlot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleLenientLanePlanSlot(item: LenientLanePlanSlot): String = classifyCappedTenderClaim(item.netVolume())
