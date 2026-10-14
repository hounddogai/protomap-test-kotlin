package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped service area draft. */
data class CappedServiceAreaDraftView(
    var codeText: String,
    var label: String,
    var maximum: String,
    var durationMinutesText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toCappedServiceAreaDraftView(item: CappedServiceAreaDraft): CappedServiceAreaDraftView {
    return CappedServiceAreaDraftView(
        codeText = item.code,
        label = item.label,
        maximum = "%.2f".format(item.maximum),
        durationMinutesText = item.durationMinutes.toString(),
        carrier = truncate(item.carrier, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedServiceAreaDraftViews(items: MutableList<CappedServiceAreaDraft>): MutableList<CappedServiceAreaDraftView> {
    val views = mutableListOf<CappedServiceAreaDraftView>()
    for (item in items) {
        views.add(toCappedServiceAreaDraftView(item))
    }
    return views
}

fun sumCappedServiceAreaDraftView(view: CappedServiceAreaDraftView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedMaximum + parsedDurationMinutes.toDouble()
}
