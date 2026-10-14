package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender draft. */
data class TenderDraftView(
    var code: String,
    var labelText: String,
    var name: String,
    var odometer: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toTenderDraftView(item: TenderDraft): TenderDraftView {
    return TenderDraftView(
        code = truncate(item.code, 80),
        labelText = item.label,
        name = item.name,
        odometer = item.odometer.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderDraftViews(items: MutableList<TenderDraft>): MutableList<TenderDraftView> {
    val views = mutableListOf<TenderDraftView>()
    for (item in items) {
        views.add(toTenderDraftView(item))
    }
    return views
}

fun sumTenderDraftView(view: TenderDraftView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedOdometer.toDouble() + parsedWindowDays.toDouble()
}
