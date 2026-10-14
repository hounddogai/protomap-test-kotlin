package com.acme.transport.lastmile.drop.dropslot

import com.acme.transport.lastmile.locker.lockerassignment.SeasonalLockerAdjustment
import com.acme.transport.lastmile.locker.lockerassignment.normalizeAdaptiveLockerAgreementCode

fun summarizeSeasonalLockerAdjustmentsForAgreement(items: MutableList<SeasonalLockerAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveSeasonalDropAgreementsFromAdjustment(items: MutableList<SeasonalLockerAdjustment>): MutableList<SeasonalDropAgreement> {
    val out = mutableListOf<SeasonalDropAgreement>()
    for (item in items) {
        val derived = newSeasonalDropAgreement(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeSeasonalDropAgreement(item: SeasonalDropAgreement): String {
    return normalizeAdaptiveLockerAgreementCode(item.code)
}
