package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.terminal.appointment.mapping.AppointmentMapping
import com.acme.transport.terminal.appointment.mapping.normalizeAppointmentBudgetCode

fun summarizeAppointmentMappingsForProposal(items: MutableList<AppointmentMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDockDoorProposalsFromMapping(items: MutableList<AppointmentMapping>): MutableList<DockDoorProposal> {
    val out = mutableListOf<DockDoorProposal>()
    for (item in items) {
        val derived = newDockDoorProposal(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignDockDoorProposal(item: DockDoorProposal): String = normalizeAppointmentBudgetCode(item.code)
