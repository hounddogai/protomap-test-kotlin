package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback inspection draft. */
data class FallbackInspectionDraftView(
    var codeText: String,
    var title: String,
    var countryText: String,
    var externalRefText: String,
    var windowDays: String,
    var etaMinutesText: String,
    var ownerId: String,
    var reviewer: String,
    var retriesText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toFallbackInspectionDraftView(item: FallbackInspectionDraft): FallbackInspectionDraftView {
    return FallbackInspectionDraftView(
        codeText = item.code,
        title = item.title,
        countryText = item.country,
        externalRefText = item.externalRef,
        windowDays = item.windowDays.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        ownerId = item.ownerId,
        reviewer = truncate(item.reviewer, 120),
        retriesText = item.retries.toString(),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackInspectionDraftViews(items: MutableList<FallbackInspectionDraft>): MutableList<FallbackInspectionDraftView> {
    val views = mutableListOf<FallbackInspectionDraftView>()
    for (item in items) {
        views.add(toFallbackInspectionDraftView(item))
    }
    return views
}

fun sumFallbackInspectionDraftView(view: FallbackInspectionDraftView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedWindowDays.toDouble() + parsedEtaMinutes.toDouble() + parsedRetries.toDouble()
}
