package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.terminal.dwell.notice.LenientDwellTemplate
import com.acme.transport.terminal.dwell.notice.normalizeDwellNoticeCode

fun summarizeLenientDwellTemplatesForAudit(items: MutableList<LenientDwellTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAppointmentAuditsFromTemplate(items: MutableList<LenientDwellTemplate>): MutableList<AppointmentAudit> {
    val out = mutableListOf<AppointmentAudit>()
    for (item in items) {
        val derived = newAppointmentAudit(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateAppointmentAudit(item: AppointmentAudit): String = normalizeDwellNoticeCode(item.code)
