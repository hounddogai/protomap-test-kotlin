package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area schedule. */
data class ServiceAreaScheduleView(
    var code: String,
    var title: String,
    var updatedAtText: String,
    var currencyText: String,
    var timezoneText: String,
    var carrier: String,
    var driverId: String,
    var region: String,
    var threshold: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toServiceAreaScheduleView(item: ServiceAreaSchedule): ServiceAreaScheduleView {
    return ServiceAreaScheduleView(
        code = item.code,
        title = item.title,
        updatedAtText = item.updatedAt.toString(),
        currencyText = item.currency,
        timezoneText = item.timezone,
        carrier = truncate(item.carrier, 40),
        driverId = item.driverId,
        region = item.region,
        threshold = "%.2f".format(item.threshold),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaScheduleViews(items: MutableList<ServiceAreaSchedule>): MutableList<ServiceAreaScheduleView> {
    val views = mutableListOf<ServiceAreaScheduleView>()
    for (item in items) {
        views.add(toServiceAreaScheduleView(item))
    }
    return views
}

fun sumServiceAreaScheduleView(view: ServiceAreaScheduleView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedUpdatedAt.toDouble() + parsedThreshold + parsedBatchSize.toDouble()
}
