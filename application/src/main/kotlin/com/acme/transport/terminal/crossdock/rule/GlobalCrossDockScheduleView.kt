package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global cross dock schedule. */
data class GlobalCrossDockScheduleView(
    var code: String,
    var titleText: String,
    var pallets: String,
    var country: String,
    var loadKgText: String,
    var nameText: String,
    var currency: String,
    var region: String,
    var graceDays: String,
    var labelText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toGlobalCrossDockScheduleView(item: GlobalCrossDockSchedule): GlobalCrossDockScheduleView {
    return GlobalCrossDockScheduleView(
        code = item.code,
        titleText = item.title,
        pallets = item.pallets.toString(),
        country = truncate(item.country, 120),
        loadKgText = "%.2f".format(item.loadKg),
        nameText = truncate(item.name, 40),
        currency = item.currency,
        region = item.region,
        graceDays = item.graceDays.toString(),
        labelText = truncate(item.label, 120),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCrossDockScheduleViews(items: MutableList<GlobalCrossDockSchedule>): MutableList<GlobalCrossDockScheduleView> {
    val views = mutableListOf<GlobalCrossDockScheduleView>()
    for (item in items) {
        views.add(toGlobalCrossDockScheduleView(item))
    }
    return views
}

fun sumGlobalCrossDockScheduleView(view: GlobalCrossDockScheduleView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedPallets.toDouble() + parsedLoadKg + parsedGraceDays.toDouble()
}
