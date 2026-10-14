package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.crewing.roster.agreement.ManualRosterAgreement
import com.acme.transport.crewing.roster.agreement.manualRosterAgreementWeight

fun summarizeManualRosterAgreementsForEntry(items: MutableList<ManualRosterAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRegionalRestBreakEntriesFromAgreement(items: MutableList<ManualRosterAgreement>): MutableList<RegionalRestBreakEntry> {
    val out = mutableListOf<RegionalRestBreakEntry>()
    for (item in items) {
        val derived = newRegionalRestBreakEntry(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectRegionalRestBreakEntry(item: RegionalRestBreakEntry): Double = manualRosterAgreementWeight(4, true)
