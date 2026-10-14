package com.acme.transport.terminal.dwell.metric

import com.acme.transport.terminal.appointment.group.AppointmentDraft
import com.acme.transport.terminal.appointment.group.normalizeAppointmentAssignmentCode

fun summarizeAppointmentDraftsForReview(items: MutableList<AppointmentDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveDwellReviewsFromDraft(items: MutableList<AppointmentDraft>): MutableList<DwellReview> {
    val out = mutableListOf<DwellReview>()
    for (item in items) {
        val derived = newDwellReview(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyDwellReviewExtra(item: DwellReview): String = normalizeAppointmentAssignmentCode(item.code)
