package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped tire summary. */
data class CappedTireSummaryView(
    var code: String,
    var title: String,
    var loadKg: String,
    var updatedAt: String,
    var unit: String,
    var notes: String,
    var label: String,
    var routeCode: String,
    var activeText: String,
    var statusLabel: String,
)

fun toCappedTireSummaryView(item: CappedTireSummary): CappedTireSummaryView {
    return CappedTireSummaryView(
        code = item.code,
        title = truncate(item.title, 80),
        loadKg = "%.2f".format(item.loadKg),
        updatedAt = item.updatedAt.toString(),
        unit = truncate(item.unit, 64),
        notes = item.notes,
        label = item.label,
        routeCode = item.routeCode,
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTireSummaryViews(items: MutableList<CappedTireSummary>): MutableList<CappedTireSummaryView> {
    val views = mutableListOf<CappedTireSummaryView>()
    for (item in items) {
        views.add(toCappedTireSummaryView(item))
    }
    return views
}

fun sumCappedTireSummaryView(view: CappedTireSummaryView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedLoadKg + parsedUpdatedAt.toDouble()
}
