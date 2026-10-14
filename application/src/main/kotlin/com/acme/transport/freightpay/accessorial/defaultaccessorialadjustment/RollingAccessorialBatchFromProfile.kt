package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.freightpay.ratecard.reservation.LenientRateCardProfile
import com.acme.transport.freightpay.ratecard.reservation.lenientRateCardProfileWeight

fun summarizeLenientRateCardProfilesForBatch(items: MutableList<LenientRateCardProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveRollingAccessorialBatchesFromProfile(items: MutableList<LenientRateCardProfile>): MutableList<RollingAccessorialBatch> {
    val out = mutableListOf<RollingAccessorialBatch>()
    for (item in items) {
        val derived = newRollingAccessorialBatch(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun assignRollingAccessorialBatch(item: RollingAccessorialBatch): Double = lenientRateCardProfileWeight(3, true)
