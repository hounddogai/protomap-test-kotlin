package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered ev charge group. */
data class TieredEvChargeGroupView(
    var code: String,
    var titleText: String,
    var rateText: String,
    var approved: String,
    var sequence: String,
    var country: String,
    var statusLabel: String,
)

fun toTieredEvChargeGroupView(item: TieredEvChargeGroup): TieredEvChargeGroupView {
    return TieredEvChargeGroupView(
        code = truncate(item.code, 64),
        titleText = item.title,
        rateText = "%.2f".format(item.rate),
        approved = "${item.approved}",
        sequence = item.sequence.toString(),
        country = truncate(item.country, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredEvChargeGroupViews(items: MutableList<TieredEvChargeGroup>): MutableList<TieredEvChargeGroupView> {
    val views = mutableListOf<TieredEvChargeGroupView>()
    for (item in items) {
        views.add(toTieredEvChargeGroupView(item))
    }
    return views
}

fun sumTieredEvChargeGroupView(view: TieredEvChargeGroupView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedRate + parsedSequence.toDouble()
}
