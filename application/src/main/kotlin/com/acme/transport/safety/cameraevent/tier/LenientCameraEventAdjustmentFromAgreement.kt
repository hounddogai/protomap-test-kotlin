package com.acme.transport.safety.cameraevent.tier

import com.acme.transport.safety.nearmiss.agreement.SeasonalNearMissAgreement
import com.acme.transport.safety.nearmiss.agreement.cappedNearMissLedgerBucket

fun summarizeSeasonalNearMissAgreementsForAdjustment(items: MutableList<SeasonalNearMissAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLenientCameraEventAdjustmentsFromAgreement(items: MutableList<SeasonalNearMissAgreement>): MutableList<LenientCameraEventAdjustment> {
    val out = mutableListOf<LenientCameraEventAdjustment>()
    for (item in items) {
        val derived = newLenientCameraEventAdjustment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveLenientCameraEventAdjustment(item: LenientCameraEventAdjustment): String {
    return cappedNearMissLedgerBucket(item.measuredValue())
}
