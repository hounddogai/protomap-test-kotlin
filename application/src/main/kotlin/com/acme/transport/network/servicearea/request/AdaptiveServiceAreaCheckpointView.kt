package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive service area checkpoint. */
data class AdaptiveServiceAreaCheckpointView(
    var code: String,
    var title: String,
    var category: String,
    var amount: String,
    var approved: String,
    var segment: String,
    var endsAt: String,
    var timezoneText: String,
    var trailerId: String,
    var carrier: String,
    var statusLabel: String,
)

fun toAdaptiveServiceAreaCheckpointView(item: AdaptiveServiceAreaCheckpoint): AdaptiveServiceAreaCheckpointView {
    return AdaptiveServiceAreaCheckpointView(
        code = truncate(item.code, 80),
        title = item.title,
        category = item.category,
        amount = "%.2f".format(item.amount),
        approved = "${item.approved}",
        segment = truncate(item.segment, 120),
        endsAt = item.endsAt.toString(),
        timezoneText = item.timezone,
        trailerId = item.trailerId,
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveServiceAreaCheckpointViews(items: MutableList<AdaptiveServiceAreaCheckpoint>): MutableList<AdaptiveServiceAreaCheckpointView> {
    val views = mutableListOf<AdaptiveServiceAreaCheckpointView>()
    for (item in items) {
        views.add(toAdaptiveServiceAreaCheckpointView(item))
    }
    return views
}

fun sumAdaptiveServiceAreaCheckpointView(view: AdaptiveServiceAreaCheckpointView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedAmount + parsedEndsAt.toDouble()
}
