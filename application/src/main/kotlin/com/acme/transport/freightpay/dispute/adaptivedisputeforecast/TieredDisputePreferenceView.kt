package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered dispute preference. */
data class TieredDisputePreferenceView(
    var code: String,
    var name: String,
    var createdAtText: String,
    var country: String,
    var carrier: String,
    var statusLabel: String,
)

fun toTieredDisputePreferenceView(item: TieredDisputePreference): TieredDisputePreferenceView {
    return TieredDisputePreferenceView(
        code = item.code,
        name = truncate(item.name, 80),
        createdAtText = item.createdAt.toString(),
        country = item.country,
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDisputePreferenceViews(items: MutableList<TieredDisputePreference>): MutableList<TieredDisputePreferenceView> {
    val views = mutableListOf<TieredDisputePreferenceView>()
    for (item in items) {
        views.add(toTieredDisputePreferenceView(item))
    }
    return views
}

fun sumTieredDisputePreferenceView(view: TieredDisputePreferenceView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedCreatedAt.toDouble()
}
