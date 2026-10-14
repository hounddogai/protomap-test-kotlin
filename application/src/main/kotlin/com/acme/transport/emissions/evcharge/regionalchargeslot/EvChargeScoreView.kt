package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge score. */
data class EvChargeScoreView(
    var code: String,
    var name: String,
    var discount: String,
    var checksum: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toEvChargeScoreView(item: EvChargeScore): EvChargeScoreView {
    return EvChargeScoreView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 64),
        discount = "%.2f".format(item.discount),
        checksum = truncate(item.checksum, 64),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeScoreViews(items: MutableList<EvChargeScore>): MutableList<EvChargeScoreView> {
    val views = mutableListOf<EvChargeScoreView>()
    for (item in items) {
        views.add(toEvChargeScoreView(item))
    }
    return views
}

fun sumEvChargeScoreView(view: EvChargeScoreView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    return parsedDiscount + parsedRatio
}
