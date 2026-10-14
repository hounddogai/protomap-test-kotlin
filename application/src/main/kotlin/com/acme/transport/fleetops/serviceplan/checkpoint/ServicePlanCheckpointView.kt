package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan checkpoint. */
data class ServicePlanCheckpointView(
    var code: String,
    var name: String,
    var driverId: String,
    var externalRef: String,
    var scoreText: String,
    var loadKg: String,
    var channelText: String,
    var dockText: String,
    var statusLabel: String,
)

fun toServicePlanCheckpointView(item: ServicePlanCheckpoint): ServicePlanCheckpointView {
    return ServicePlanCheckpointView(
        code = truncate(item.code, 80),
        name = item.name,
        driverId = truncate(item.driverId, 40),
        externalRef = item.externalRef,
        scoreText = "%.2f".format(item.score),
        loadKg = "%.2f".format(item.loadKg),
        channelText = item.channel,
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanCheckpointViews(items: MutableList<ServicePlanCheckpoint>): MutableList<ServicePlanCheckpointView> {
    val views = mutableListOf<ServicePlanCheckpointView>()
    for (item in items) {
        views.add(toServicePlanCheckpointView(item))
    }
    return views
}

fun sumServicePlanCheckpointView(view: ServicePlanCheckpointView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedScore + parsedLoadKg
}
