package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled swap digest. */
data class ScheduledSwapDigestView(
    var code: String,
    var title: String,
    var discount: String,
    var maximumText: String,
    var ratioText: String,
    var sequenceText: String,
    var version: String,
    var fuelLiters: String,
    var depotText: String,
    var name: String,
    var statusLabel: String,
)

fun toScheduledSwapDigestView(item: ScheduledSwapDigest): ScheduledSwapDigestView {
    return ScheduledSwapDigestView(
        code = item.code,
        title = item.title,
        discount = "%.2f".format(item.discount),
        maximumText = "%.2f".format(item.maximum),
        ratioText = "%.2f".format(item.ratio),
        sequenceText = item.sequence.toString(),
        version = item.version.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        depotText = item.depot,
        name = truncate(item.name, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledSwapDigestViews(items: MutableList<ScheduledSwapDigest>): MutableList<ScheduledSwapDigestView> {
    val views = mutableListOf<ScheduledSwapDigestView>()
    for (item in items) {
        views.add(toScheduledSwapDigestView(item))
    }
    return views
}

fun sumScheduledSwapDigestView(view: ScheduledSwapDigestView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedDiscount + parsedMaximum + parsedRatio
}
