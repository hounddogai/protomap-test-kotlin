package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul bundle. */
data class BackhaulBundleView(
    var code: String,
    var labelText: String,
    var ceilingText: String,
    var title: String,
    var timezone: String,
    var externalRef: String,
    var routeCode: String,
    var amount: String,
    var dock: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toBackhaulBundleView(item: BackhaulBundle): BackhaulBundleView {
    return BackhaulBundleView(
        code = item.code,
        labelText = truncate(item.label, 80),
        ceilingText = "%.2f".format(item.ceiling),
        title = item.title,
        timezone = item.timezone,
        externalRef = item.externalRef,
        routeCode = item.routeCode,
        amount = "%.2f".format(item.amount),
        dock = item.dock,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulBundleViews(items: MutableList<BackhaulBundle>): MutableList<BackhaulBundleView> {
    val views = mutableListOf<BackhaulBundleView>()
    for (item in items) {
        views.add(toBackhaulBundleView(item))
    }
    return views
}

fun sumBackhaulBundleView(view: BackhaulBundleView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedCeiling + parsedAmount + parsedFuelLiters
}
