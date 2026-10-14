package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.terminal.appointment.exception.AppointmentException
import com.acme.transport.terminal.appointment.exception.classifyPrimaryAppointmentQuota

fun summarizeAppointmentExceptionsForClaim(items: MutableList<AppointmentException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRegionalSortationClaimsFromException(items: MutableList<AppointmentException>): MutableList<RegionalSortationClaim> {
    val out = mutableListOf<RegionalSortationClaim>()
    for (item in items) {
        val derived = newRegionalSortationClaim(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planRegionalSortationClaim(item: RegionalSortationClaim): String {
    return classifyPrimaryAppointmentQuota(item.projectedVolume())
}
