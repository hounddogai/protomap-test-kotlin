package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery tier. */
data class RedeliveryTierView(
    var code: String,
    var title: String,
    var fuelLiters: String,
    var source: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toRedeliveryTierView(item: RedeliveryTier): RedeliveryTierView {
    return RedeliveryTierView(
        code = item.code,
        title = truncate(item.title, 80),
        fuelLiters = "%.2f".format(item.fuelLiters),
        source = item.source,
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryTierViews(items: MutableList<RedeliveryTier>): MutableList<RedeliveryTierView> {
    val views = mutableListOf<RedeliveryTierView>()
    for (item in items) {
        views.add(toRedeliveryTierView(item))
    }
    return views
}

fun sumRedeliveryTierView(view: RedeliveryTierView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedFuelLiters + parsedWindowDays.toDouble()
}
