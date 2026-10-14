package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.freightpay.ratecard.rule.RateCardProfile
import com.acme.transport.freightpay.ratecard.rule.adaptiveRateCardReservationWeight

fun summarizeRateCardProfilesForRecord(items: MutableList<RateCardProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAccessorialRecordsFromProfile(items: MutableList<RateCardProfile>): MutableList<AccessorialRecord> {
    val out = mutableListOf<AccessorialRecord>()
    for (item in items) {
        val derived = newAccessorialRecord(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignAccessorialRecord(item: AccessorialRecord): Double = adaptiveRateCardReservationWeight(1, true)
