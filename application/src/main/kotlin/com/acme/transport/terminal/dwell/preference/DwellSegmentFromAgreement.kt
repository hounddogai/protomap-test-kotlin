package com.acme.transport.terminal.dwell.preference

import com.acme.transport.terminal.dockdoor.dooragreement.RollingDockDoorAgreement
import com.acme.transport.terminal.dockdoor.dooragreement.normalizeRollingDockDoorAgreementCode

fun summarizeRollingDockDoorAgreementsForSegment(items: MutableList<RollingDockDoorAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDwellSegmentsFromAgreement(items: MutableList<RollingDockDoorAgreement>): MutableList<DwellSegment> {
    val out = mutableListOf<DwellSegment>()
    for (item in items) {
        val derived = newDwellSegment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceDwellSegment(item: DwellSegment): String = normalizeRollingDockDoorAgreementCode(item.code)
