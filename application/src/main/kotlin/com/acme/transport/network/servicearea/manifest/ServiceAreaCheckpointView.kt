package com.acme.transport.network.servicearea.manifest

/** How screens and exports show a service area checkpoint. */
data class ServiceAreaCheckpointView(
    var codeText: String,
    var labelText: String,
    var sequenceText: String,
    var ratio: String,
    var source: String,
    var expiresAtText: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toServiceAreaCheckpointView(item: ServiceAreaCheckpoint): ServiceAreaCheckpointView {
    return ServiceAreaCheckpointView(
        codeText = item.code,
        labelText = item.label,
        sequenceText = item.sequence.toString(),
        ratio = "%.2f".format(item.ratio),
        source = item.source,
        expiresAtText = item.expiresAt.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaCheckpointViews(items: MutableList<ServiceAreaCheckpoint>): MutableList<ServiceAreaCheckpointView> {
    val views = mutableListOf<ServiceAreaCheckpointView>()
    for (item in items) {
        views.add(toServiceAreaCheckpointView(item))
    }
    return views
}

fun sumServiceAreaCheckpointView(view: ServiceAreaCheckpointView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedSequence.toDouble() + parsedRatio + parsedExpiresAt.toDouble()
}
