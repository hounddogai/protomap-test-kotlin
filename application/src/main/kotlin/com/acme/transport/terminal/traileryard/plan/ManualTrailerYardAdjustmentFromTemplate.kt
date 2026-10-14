package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.terminal.appointment.appointmenttemplate.CappedAppointmentTemplate
import com.acme.transport.terminal.appointment.appointmenttemplate.classifyCappedAppointmentTemplate

fun summarizeCappedAppointmentTemplatesForAdjustment(items: MutableList<CappedAppointmentTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveManualTrailerYardAdjustmentsFromTemplate(items: MutableList<CappedAppointmentTemplate>): MutableList<ManualTrailerYardAdjustment> {
    val out = mutableListOf<ManualTrailerYardAdjustment>()
    for (item in items) {
        val derived = newManualTrailerYardAdjustment(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyManualTrailerYardAdjustment(item: ManualTrailerYardAdjustment): String {
    return classifyCappedAppointmentTemplate(item.projectedTotal())
}
