package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area segment. */
data class ServiceAreaSegmentView(
    var code: String,
    var nameText: String,
    var threshold: String,
    var country: String,
    var marginText: String,
    var carrier: String,
    var timezone: String,
    var visible: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toServiceAreaSegmentView(item: ServiceAreaSegment): ServiceAreaSegmentView {
    return ServiceAreaSegmentView(
        code = item.code,
        nameText = truncate(item.name, 80),
        threshold = "%.2f".format(item.threshold),
        country = truncate(item.country, 120),
        marginText = "%.2f".format(item.margin),
        carrier = item.carrier,
        timezone = item.timezone,
        visible = "${item.visible}",
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaSegmentViews(items: MutableList<ServiceAreaSegment>): MutableList<ServiceAreaSegmentView> {
    val views = mutableListOf<ServiceAreaSegmentView>()
    for (item in items) {
        views.add(toServiceAreaSegmentView(item))
    }
    return views
}

fun sumServiceAreaSegmentView(view: ServiceAreaSegmentView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedMargin = view.marginText.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedThreshold + parsedMargin + parsedBatchSize.toDouble()
}
