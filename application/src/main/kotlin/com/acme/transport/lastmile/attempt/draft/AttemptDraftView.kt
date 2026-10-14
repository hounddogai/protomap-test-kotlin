package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt draft. */
data class AttemptDraftView(
    var code: String,
    var title: String,
    var pallets: String,
    var odometer: String,
    var ratio: String,
    var dock: String,
    var depotText: String,
    var statusLabel: String,
)

fun toAttemptDraftView(item: AttemptDraft): AttemptDraftView {
    return AttemptDraftView(
        code = truncate(item.code, 40),
        title = item.title,
        pallets = item.pallets.toString(),
        odometer = item.odometer.toString(),
        ratio = "%.2f".format(item.ratio),
        dock = item.dock,
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptDraftViews(items: MutableList<AttemptDraft>): MutableList<AttemptDraftView> {
    val views = mutableListOf<AttemptDraftView>()
    for (item in items) {
        views.add(toAttemptDraftView(item))
    }
    return views
}

fun sumAttemptDraftView(view: AttemptDraftView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedPallets.toDouble() + parsedOdometer.toDouble() + parsedRatio
}
