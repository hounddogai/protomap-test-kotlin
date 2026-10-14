package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.terminal.traileryard.target.RollingTrailerYardReview
import com.acme.transport.terminal.traileryard.target.classifyLegacyTrailerYardAgreement

fun summarizeRollingTrailerYardReviewsForTier(items: MutableList<RollingTrailerYardReview>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveAppointmentTiersFromReview(items: MutableList<RollingTrailerYardReview>): MutableList<AppointmentTier> {
    val out = mutableListOf<AppointmentTier>()
    for (item in items) {
        val derived = newAppointmentTier(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun planAppointmentTier(item: AppointmentTier): String = classifyLegacyTrailerYardAgreement(item.netLoad())
