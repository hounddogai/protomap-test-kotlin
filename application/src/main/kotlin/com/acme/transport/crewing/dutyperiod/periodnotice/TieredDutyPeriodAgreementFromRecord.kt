package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.crewing.roster.tier.RosterRecord
import com.acme.transport.crewing.roster.tier.rosterEntryWeight

fun summarizeRosterRecordsForAgreement(items: MutableList<RosterRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveTieredDutyPeriodAgreementsFromRecord(items: MutableList<RosterRecord>): MutableList<TieredDutyPeriodAgreement> {
    val out = mutableListOf<TieredDutyPeriodAgreement>()
    for (item in items) {
        val derived = newTieredDutyPeriodAgreement(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeTieredDutyPeriodAgreement(item: TieredDutyPeriodAgreement): Double = rosterEntryWeight(8, true)
