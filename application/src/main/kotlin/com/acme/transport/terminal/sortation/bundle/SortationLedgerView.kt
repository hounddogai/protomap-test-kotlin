package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation ledger. */
data class SortationLedgerView(
    var code: String,
    var title: String,
    var approved: String,
    var maximum: String,
    var distanceKm: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toSortationLedgerView(item: SortationLedger): SortationLedgerView {
    return SortationLedgerView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 40),
        approved = "${item.approved}",
        maximum = "%.2f".format(item.maximum),
        distanceKm = "%.2f".format(item.distanceKm),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationLedgerViews(items: MutableList<SortationLedger>): MutableList<SortationLedgerView> {
    val views = mutableListOf<SortationLedgerView>()
    for (item in items) {
        views.add(toSortationLedgerView(item))
    }
    return views
}

fun sumSortationLedgerView(view: SortationLedgerView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedMaximum + parsedDistanceKm
}
