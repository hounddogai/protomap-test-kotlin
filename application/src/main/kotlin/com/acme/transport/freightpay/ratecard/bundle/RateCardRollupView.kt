package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card rollup. */
data class RateCardRollupView(
    var codeText: String,
    var label: String,
    var notesText: String,
    var pallets: String,
    var categoryText: String,
    var maximumText: String,
    var minimum: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toRateCardRollupView(item: RateCardRollup): RateCardRollupView {
    return RateCardRollupView(
        codeText = truncate(item.code, 40),
        label = item.label,
        notesText = truncate(item.notes, 120),
        pallets = item.pallets.toString(),
        categoryText = truncate(item.category, 64),
        maximumText = "%.2f".format(item.maximum),
        minimum = "%.2f".format(item.minimum),
        segmentText = truncate(item.segment, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardRollupViews(items: MutableList<RateCardRollup>): MutableList<RateCardRollupView> {
    val views = mutableListOf<RateCardRollupView>()
    for (item in items) {
        views.add(toRateCardRollupView(item))
    }
    return views
}

fun sumRateCardRollupView(view: RateCardRollupView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedPallets.toDouble() + parsedMaximum + parsedMinimum
}
