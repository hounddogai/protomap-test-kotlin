package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect baseline. */
data class DefectBaselineView(
    var code: String,
    var label: String,
    var visibleText: String,
    var amount: String,
    var name: String,
    var timezone: String,
    var durationMinutes: String,
    var reviewerText: String,
    var localeText: String,
    var expiresAt: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toDefectBaselineView(item: DefectBaseline): DefectBaselineView {
    return DefectBaselineView(
        code = item.code,
        label = truncate(item.label, 80),
        visibleText = "${item.visible}",
        amount = "%.2f".format(item.amount),
        name = truncate(item.name, 120),
        timezone = truncate(item.timezone, 80),
        durationMinutes = item.durationMinutes.toString(),
        reviewerText = item.reviewer,
        localeText = truncate(item.locale, 120),
        expiresAt = item.expiresAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectBaselineViews(items: MutableList<DefectBaseline>): MutableList<DefectBaselineView> {
    val views = mutableListOf<DefectBaselineView>()
    for (item in items) {
        views.add(toDefectBaselineView(item))
    }
    return views
}

fun sumDefectBaselineView(view: DefectBaselineView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedAmount + parsedDurationMinutes.toDouble() + parsedExpiresAt.toDouble()
}
