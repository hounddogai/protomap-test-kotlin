package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.terminal.appointment.rollup.LenientAppointmentAudit
import com.acme.transport.terminal.appointment.rollup.lenientAppointmentAuditWeight

fun summarizeLenientAppointmentAuditsForTemplate(items: MutableList<LenientAppointmentAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveRollingDwellTemplatesFromAudit(items: MutableList<LenientAppointmentAudit>): MutableList<RollingDwellTemplate> {
    val out = mutableListOf<RollingDwellTemplate>()
    for (item in items) {
        val derived = newRollingDwellTemplate(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeRollingDwellTemplate(item: RollingDwellTemplate): Double = lenientAppointmentAuditWeight(8, true)
