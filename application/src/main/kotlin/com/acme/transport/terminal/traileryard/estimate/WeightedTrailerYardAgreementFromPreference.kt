package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.terminal.appointment.appointmentpreference.CappedAppointmentPreference
import com.acme.transport.terminal.appointment.appointmentpreference.normalizeAppointmentDigestCode

fun summarizeCappedAppointmentPreferencesForAgreement(items: MutableList<CappedAppointmentPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveWeightedTrailerYardAgreementsFromPreference(items: MutableList<CappedAppointmentPreference>): MutableList<WeightedTrailerYardAgreement> {
    val out = mutableListOf<WeightedTrailerYardAgreement>()
    for (item in items) {
        val derived = newWeightedTrailerYardAgreement(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyWeightedTrailerYardAgreementExtra(item: WeightedTrailerYardAgreement): String {
    return normalizeAppointmentDigestCode(item.code)
}
