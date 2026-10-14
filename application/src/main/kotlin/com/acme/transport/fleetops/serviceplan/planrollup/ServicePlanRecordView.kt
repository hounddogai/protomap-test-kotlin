package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan record. */
data class ServicePlanRecordView(
    var codeText: String,
    var nameText: String,
    var limitText: String,
    var distanceKm: String,
    var region: String,
    var stopCount: String,
    var capacity: String,
    var batchSize: String,
    var palletsText: String,
    var segment: String,
    var statusLabel: String,
)

fun toServicePlanRecordView(item: ServicePlanRecord): ServicePlanRecordView {
    return ServicePlanRecordView(
        codeText = item.code,
        nameText = item.name,
        limitText = item.limit.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        region = truncate(item.region, 64),
        stopCount = item.stopCount.toString(),
        capacity = item.capacity.toString(),
        batchSize = item.batchSize.toString(),
        palletsText = item.pallets.toString(),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanRecordViews(items: MutableList<ServicePlanRecord>): MutableList<ServicePlanRecordView> {
    val views = mutableListOf<ServicePlanRecordView>()
    for (item in items) {
        views.add(toServicePlanRecordView(item))
    }
    return views
}

fun sumServicePlanRecordView(view: ServicePlanRecordView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedLimit.toDouble() + parsedDistanceKm + parsedStopCount.toDouble()
}
