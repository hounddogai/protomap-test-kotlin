package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped service area profile. */
data class CappedServiceAreaProfileView(
    var codeText: String,
    var name: String,
    var batchSize: String,
    var distanceKm: String,
    var channel: String,
    var cost: String,
    var label: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toCappedServiceAreaProfileView(item: CappedServiceAreaProfile): CappedServiceAreaProfileView {
    return CappedServiceAreaProfileView(
        codeText = item.code,
        name = item.name,
        batchSize = item.batchSize.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        channel = truncate(item.channel, 80),
        cost = "%.2f".format(item.cost),
        label = truncate(item.label, 80),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedServiceAreaProfileViews(items: MutableList<CappedServiceAreaProfile>): MutableList<CappedServiceAreaProfileView> {
    val views = mutableListOf<CappedServiceAreaProfileView>()
    for (item in items) {
        views.add(toCappedServiceAreaProfileView(item))
    }
    return views
}

fun sumCappedServiceAreaProfileView(view: CappedServiceAreaProfileView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedBatchSize.toDouble() + parsedDistanceKm + parsedCost
}
