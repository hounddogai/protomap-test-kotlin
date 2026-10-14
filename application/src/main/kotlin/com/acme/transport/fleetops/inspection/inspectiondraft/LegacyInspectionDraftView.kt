package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy inspection draft. */
data class LegacyInspectionDraftView(
    var code: String,
    var label: String,
    var version: String,
    var odometer: String,
    var countryText: String,
    var sequenceText: String,
    var trailerIdText: String,
    var statusLabel: String,
)

fun toLegacyInspectionDraftView(item: LegacyInspectionDraft): LegacyInspectionDraftView {
    return LegacyInspectionDraftView(
        code = item.code,
        label = item.label,
        version = item.version.toString(),
        odometer = item.odometer.toString(),
        countryText = item.country,
        sequenceText = item.sequence.toString(),
        trailerIdText = truncate(item.trailerId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyInspectionDraftViews(items: MutableList<LegacyInspectionDraft>): MutableList<LegacyInspectionDraftView> {
    val views = mutableListOf<LegacyInspectionDraftView>()
    for (item in items) {
        views.add(toLegacyInspectionDraftView(item))
    }
    return views
}

fun sumLegacyInspectionDraftView(view: LegacyInspectionDraftView): Double {
    val parsedVersion = view.version.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedVersion.toDouble() + parsedOdometer.toDouble() + parsedSequence.toDouble()
}
