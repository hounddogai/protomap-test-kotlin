package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock mapping. */
data class CrossDockMappingView(
    var codeText: String,
    var title: String,
    var durationMinutes: String,
    var approved: String,
    var discount: String,
    var unit: String,
    var graceDays: String,
    var cost: String,
    var statusLabel: String,
)

fun toCrossDockMappingView(item: CrossDockMapping): CrossDockMappingView {
    return CrossDockMappingView(
        codeText = item.code,
        title = truncate(item.title, 80),
        durationMinutes = item.durationMinutes.toString(),
        approved = "${item.approved}",
        discount = "%.2f".format(item.discount),
        unit = truncate(item.unit, 40),
        graceDays = item.graceDays.toString(),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockMappingViews(items: MutableList<CrossDockMapping>): MutableList<CrossDockMappingView> {
    val views = mutableListOf<CrossDockMappingView>()
    for (item in items) {
        views.add(toCrossDockMappingView(item))
    }
    return views
}

fun sumCrossDockMappingView(view: CrossDockMappingView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedDurationMinutes.toDouble() + parsedDiscount + parsedGraceDays.toDouble()
}
