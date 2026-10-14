package com.acme.transport.terminal.appointment.segment

import com.acme.transport.terminal.appointment.rollup.AppointmentMetric
import com.acme.transport.terminal.appointment.rollup.lenientAppointmentAuditWeight

fun summarizeAppointmentMetricsForAudit(items: MutableList<AppointmentMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveSeasonalAppointmentAuditsFromMetric(items: MutableList<AppointmentMetric>): MutableList<SeasonalAppointmentAudit> {
    val out = mutableListOf<SeasonalAppointmentAudit>()
    for (item in items) {
        val derived = newSeasonalAppointmentAudit(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreSeasonalAppointmentAudit(item: SeasonalAppointmentAudit): Double = lenientAppointmentAuditWeight(9, true)
