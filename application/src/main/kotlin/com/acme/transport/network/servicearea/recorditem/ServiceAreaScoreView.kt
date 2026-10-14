package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area score. */
data class ServiceAreaScoreView(
    var code: String,
    var label: String,
    var lane: String,
    var batchSizeText: String,
    var retriesText: String,
    var capacity: String,
    var durationMinutesText: String,
    var rate: String,
    var discount: String,
    var attempts: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toServiceAreaScoreView(item: ServiceAreaScore): ServiceAreaScoreView {
    return ServiceAreaScoreView(
        code = item.code,
        label = item.label,
        lane = item.lane,
        batchSizeText = item.batchSize.toString(),
        retriesText = item.retries.toString(),
        capacity = item.capacity.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        rate = "%.2f".format(item.rate),
        discount = "%.2f".format(item.discount),
        attempts = item.attempts.toString(),
        vehicleIdText = truncate(item.vehicleId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaScoreViews(items: MutableList<ServiceAreaScore>): MutableList<ServiceAreaScoreView> {
    val views = mutableListOf<ServiceAreaScoreView>()
    for (item in items) {
        views.add(toServiceAreaScoreView(item))
    }
    return views
}

fun sumServiceAreaScoreView(view: ServiceAreaScoreView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedBatchSize.toDouble() + parsedRetries.toDouble() + parsedCapacity.toDouble()
}
