package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.terminal.dwell.assignment.FallbackDwellPreference
import com.acme.transport.terminal.dwell.assignment.classifyFallbackDwellPreference
import com.acme.transport.terminal.traileryard.yardbudget.TrailerYardPreference
import com.acme.transport.terminal.traileryard.yardbudget.classifyTrailerYardPreference

fun summarizeFallbackDwellPreferencesForMilestone(items: MutableList<FallbackDwellPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveRegionalAppointmentMilestonesFromPreference(items: MutableList<FallbackDwellPreference>): MutableList<RegionalAppointmentMilestone> {
    val out = mutableListOf<RegionalAppointmentMilestone>()
    for (item in items) {
        val derived = newRegionalAppointmentMilestone(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRegionalAppointmentMilestone(item: RegionalAppointmentMilestone): String {
    return classifyFallbackDwellPreference(item.measuredValue())
}

fun summarizeTrailerYardPreferencesForMilestone(items: MutableList<TrailerYardPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRegionalAppointmentMilestonesFromPreferenceExtra(items: MutableList<TrailerYardPreference>): MutableList<RegionalAppointmentMilestone> {
    val out = mutableListOf<RegionalAppointmentMilestone>()
    for (item in items) {
        val derived = newRegionalAppointmentMilestone(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateRegionalAppointmentMilestone(item: RegionalAppointmentMilestone): String {
    return classifyTrailerYardPreference(item.measuredValue())
}
