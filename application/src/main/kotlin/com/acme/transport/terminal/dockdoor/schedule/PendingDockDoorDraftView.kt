package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending dock door draft. */
data class PendingDockDoorDraftView(
    var codeText: String,
    var title: String,
    var loadKg: String,
    var margin: String,
    var archived: String,
    var version: String,
    var pallets: String,
    var odometer: String,
    var amount: String,
    var lane: String,
    var statusLabel: String,
)

fun toPendingDockDoorDraftView(item: PendingDockDoorDraft): PendingDockDoorDraftView {
    return PendingDockDoorDraftView(
        codeText = item.code,
        title = item.title,
        loadKg = "%.2f".format(item.loadKg),
        margin = "%.2f".format(item.margin),
        archived = "${item.archived}",
        version = item.version.toString(),
        pallets = item.pallets.toString(),
        odometer = item.odometer.toString(),
        amount = "%.2f".format(item.amount),
        lane = truncate(item.lane, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDockDoorDraftViews(items: MutableList<PendingDockDoorDraft>): MutableList<PendingDockDoorDraftView> {
    val views = mutableListOf<PendingDockDoorDraftView>()
    for (item in items) {
        views.add(toPendingDockDoorDraftView(item))
    }
    return views
}

fun sumPendingDockDoorDraftView(view: PendingDockDoorDraftView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedLoadKg + parsedMargin + parsedVersion.toDouble()
}
