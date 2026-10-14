package com.acme.transport.terminal.appointment.policy

import com.acme.transport.terminal.sortation.window.LenientSortationWindow
import com.acme.transport.terminal.sortation.window.normalizeRollingSortationWindowCode

fun summarizeLenientSortationWindowsForPolicy(items: MutableList<LenientSortationWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveAppointmentPoliciesFromWindow(items: MutableList<LenientSortationWindow>): MutableList<AppointmentPolicy> {
    val out = mutableListOf<AppointmentPolicy>()
    for (item in items) {
        val derived = newAppointmentPolicy(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveAppointmentPolicy(item: AppointmentPolicy): String = normalizeRollingSortationWindowCode(item.code)
