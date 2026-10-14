package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default rest break variance. */
data class DefaultRestBreakVarianceView(
    var codeText: String,
    var title: String,
    var odometer: String,
    var notes: String,
    var pinnedText: String,
    var stopCountText: String,
    var depot: String,
    var amount: String,
    var batchSizeText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toDefaultRestBreakVarianceView(item: DefaultRestBreakVariance): DefaultRestBreakVarianceView {
    return DefaultRestBreakVarianceView(
        codeText = truncate(item.code, 64),
        title = item.title,
        odometer = item.odometer.toString(),
        notes = item.notes,
        pinnedText = "${item.pinned}",
        stopCountText = item.stopCount.toString(),
        depot = item.depot,
        amount = "%.2f".format(item.amount),
        batchSizeText = item.batchSize.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRestBreakVarianceViews(items: MutableList<DefaultRestBreakVariance>): MutableList<DefaultRestBreakVarianceView> {
    val views = mutableListOf<DefaultRestBreakVarianceView>()
    for (item in items) {
        views.add(toDefaultRestBreakVarianceView(item))
    }
    return views
}

fun sumDefaultRestBreakVarianceView(view: DefaultRestBreakVarianceView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedOdometer.toDouble() + parsedStopCount.toDouble() + parsedAmount
}
