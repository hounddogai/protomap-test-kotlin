package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff window. */
data class CutoffWindowView(
    var code: String,
    var labelText: String,
    var lane: String,
    var costText: String,
    var depot: String,
    var statusLabel: String,
)

fun toCutoffWindowView(item: CutoffWindow): CutoffWindowView {
    return CutoffWindowView(
        code = item.code,
        labelText = item.label,
        lane = truncate(item.lane, 64),
        costText = "%.2f".format(item.cost),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffWindowViews(items: MutableList<CutoffWindow>): MutableList<CutoffWindowView> {
    val views = mutableListOf<CutoffWindowView>()
    for (item in items) {
        views.add(toCutoffWindowView(item))
    }
    return views
}

fun sumCutoffWindowView(view: CutoffWindowView): Double {
    val parsedCost = view.costText.toDouble()
    return parsedCost
}
