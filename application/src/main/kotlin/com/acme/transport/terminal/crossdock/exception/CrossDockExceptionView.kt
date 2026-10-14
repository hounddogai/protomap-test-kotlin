package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock exception. */
data class CrossDockExceptionView(
    var code: String,
    var nameText: String,
    var amount: String,
    var reviewer: String,
    var durationMinutes: String,
    var fuelLiters: String,
    var ratio: String,
    var stopCountText: String,
    var sourceText: String,
    var countryText: String,
    var regionText: String,
    var statusLabel: String,
)

fun toCrossDockExceptionView(item: CrossDockException): CrossDockExceptionView {
    return CrossDockExceptionView(
        code = item.code,
        nameText = item.name,
        amount = "%.2f".format(item.amount),
        reviewer = item.reviewer,
        durationMinutes = item.durationMinutes.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        ratio = "%.2f".format(item.ratio),
        stopCountText = item.stopCount.toString(),
        sourceText = item.source,
        countryText = truncate(item.country, 120),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockExceptionViews(items: MutableList<CrossDockException>): MutableList<CrossDockExceptionView> {
    val views = mutableListOf<CrossDockExceptionView>()
    for (item in items) {
        views.add(toCrossDockExceptionView(item))
    }
    return views
}

fun sumCrossDockExceptionView(view: CrossDockExceptionView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedAmount + parsedDurationMinutes.toDouble() + parsedFuelLiters
}
