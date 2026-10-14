package com.acme.transport.terminal.crossdock.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock template. */
data class CrossDockTemplateView(
    var code: String,
    var nameText: String,
    var unitText: String,
    var locale: String,
    var depot: String,
    var title: String,
    var pinned: String,
    var maximum: String,
    var pallets: String,
    var minimum: String,
    var statusLabel: String,
)

fun toCrossDockTemplateView(item: CrossDockTemplate): CrossDockTemplateView {
    return CrossDockTemplateView(
        code = item.code,
        nameText = item.name,
        unitText = item.unit,
        locale = item.locale,
        depot = truncate(item.depot, 64),
        title = item.title,
        pinned = "${item.pinned}",
        maximum = "%.2f".format(item.maximum),
        pallets = item.pallets.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockTemplateViews(items: MutableList<CrossDockTemplate>): MutableList<CrossDockTemplateView> {
    val views = mutableListOf<CrossDockTemplateView>()
    for (item in items) {
        views.add(toCrossDockTemplateView(item))
    }
    return views
}

fun sumCrossDockTemplateView(view: CrossDockTemplateView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedMaximum + parsedPallets.toDouble() + parsedMinimum
}
