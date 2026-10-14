package com.acme.transport.crewing.roster.schedule

import com.acme.transport.crewing.roster.agreement.ManualRosterAgreement
import com.acme.transport.crewing.roster.agreement.normalizeBatchedRosterAdjustmentCode

fun summarizeManualRosterAgreementsForSchedule(items: MutableList<ManualRosterAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRosterSchedulesFromAgreement(items: MutableList<ManualRosterAgreement>): MutableList<RosterSchedule> {
    val out = mutableListOf<RosterSchedule>()
    for (item in items) {
        val derived = newRosterSchedule(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitRosterSchedule(item: RosterSchedule): String = normalizeBatchedRosterAdjustmentCode(item.code)
