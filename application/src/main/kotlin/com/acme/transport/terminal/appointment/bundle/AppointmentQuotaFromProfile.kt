package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.terminal.crossdock.notice.CrossDockProfile
import com.acme.transport.terminal.crossdock.notice.normalizePrimaryCrossDockRecordCode

fun summarizeCrossDockProfilesForQuota(items: MutableList<CrossDockProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveAppointmentQuotasFromProfile(items: MutableList<CrossDockProfile>): MutableList<AppointmentQuota> {
    val out = mutableListOf<AppointmentQuota>()
    for (item in items) {
        val derived = newAppointmentQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateAppointmentQuota(item: AppointmentQuota): String = normalizePrimaryCrossDockRecordCode(item.code)
