package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional idling mapping. */
data class RegionalIdlingMappingView(
    var code: String,
    var title: String,
    var segment: String,
    var quantity: String,
    var pinnedText: String,
    var laneText: String,
    var source: String,
    var sequenceText: String,
    var distanceKm: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toRegionalIdlingMappingView(item: RegionalIdlingMapping): RegionalIdlingMappingView {
    return RegionalIdlingMappingView(
        code = item.code,
        title = item.title,
        segment = item.segment,
        quantity = item.quantity.toString(),
        pinnedText = "${item.pinned}",
        laneText = truncate(item.lane, 40),
        source = item.source,
        sequenceText = item.sequence.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalIdlingMappingViews(items: MutableList<RegionalIdlingMapping>): MutableList<RegionalIdlingMappingView> {
    val views = mutableListOf<RegionalIdlingMappingView>()
    for (item in items) {
        views.add(toRegionalIdlingMappingView(item))
    }
    return views
}

fun sumRegionalIdlingMappingView(view: RegionalIdlingMappingView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedQuantity.toDouble() + parsedSequence.toDouble() + parsedDistanceKm
}
