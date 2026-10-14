package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance target. */
data class ClearanceTargetView(
    var code: String,
    var title: String,
    var label: String,
    var reviewer: String,
    var source: String,
    var archived: String,
    var margin: String,
    var pallets: String,
    var statusLabel: String,
)

fun toClearanceTargetView(item: ClearanceTarget): ClearanceTargetView {
    return ClearanceTargetView(
        code = truncate(item.code, 120),
        title = item.title,
        label = item.label,
        reviewer = item.reviewer,
        source = item.source,
        archived = "${item.archived}",
        margin = "%.2f".format(item.margin),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceTargetViews(items: MutableList<ClearanceTarget>): MutableList<ClearanceTargetView> {
    val views = mutableListOf<ClearanceTargetView>()
    for (item in items) {
        views.add(toClearanceTargetView(item))
    }
    return views
}

fun sumClearanceTargetView(view: ClearanceTargetView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedMargin + parsedPallets.toDouble()
}
