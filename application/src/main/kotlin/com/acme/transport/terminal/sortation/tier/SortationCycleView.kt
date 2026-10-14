package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation cycle. */
data class SortationCycleView(
    var codeText: String,
    var title: String,
    var label: String,
    var discountText: String,
    var score: String,
    var odometer: String,
    var carrier: String,
    var statusLabel: String,
)

fun toSortationCycleView(item: SortationCycle): SortationCycleView {
    return SortationCycleView(
        codeText = item.code,
        title = item.title,
        label = truncate(item.label, 120),
        discountText = "%.2f".format(item.discount),
        score = "%.2f".format(item.score),
        odometer = item.odometer.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationCycleViews(items: MutableList<SortationCycle>): MutableList<SortationCycleView> {
    val views = mutableListOf<SortationCycleView>()
    for (item in items) {
        views.add(toSortationCycleView(item))
    }
    return views
}

fun sumSortationCycleView(view: SortationCycleView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedDiscount + parsedScore + parsedOdometer.toDouble()
}
