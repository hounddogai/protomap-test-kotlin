package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.crewing.dutyperiod.draft.TieredDutyPeriodRollup
import com.acme.transport.crewing.dutyperiod.draft.dutyPeriodDraftWeight

fun summarizeTieredDutyPeriodRollupsForProfile(items: MutableList<TieredDutyPeriodRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLenientDutyPeriodProfilesFromRollup(items: MutableList<TieredDutyPeriodRollup>): MutableList<LenientDutyPeriodProfile> {
    val out = mutableListOf<LenientDutyPeriodProfile>()
    for (item in items) {
        val derived = newLenientDutyPeriodProfile(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleLenientDutyPeriodProfile(item: LenientDutyPeriodProfile): Double = dutyPeriodDraftWeight(6, true)
