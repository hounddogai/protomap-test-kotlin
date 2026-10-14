package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global roster group. */
data class GlobalRosterGroupView(
    var code: String,
    var title: String,
    var summary: String,
    var dock: String,
    var vehicleId: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toGlobalRosterGroupView(item: GlobalRosterGroup): GlobalRosterGroupView {
    return GlobalRosterGroupView(
        code = item.code,
        title = truncate(item.title, 64),
        summary = truncate(item.summary, 40),
        dock = item.dock,
        vehicleId = truncate(item.vehicleId, 64),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRosterGroupViews(items: MutableList<GlobalRosterGroup>): MutableList<GlobalRosterGroupView> {
    val views = mutableListOf<GlobalRosterGroupView>()
    for (item in items) {
        views.add(toGlobalRosterGroupView(item))
    }
    return views
}

fun sumGlobalRosterGroupView(view: GlobalRosterGroupView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
