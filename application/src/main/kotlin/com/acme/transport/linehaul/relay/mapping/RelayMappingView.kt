package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay mapping. */
data class RelayMappingView(
    var code: String,
    var label: String,
    var rate: String,
    var stopCount: String,
    var revisionText: String,
    var loadKg: String,
    var trailerId: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toRelayMappingView(item: RelayMapping): RelayMappingView {
    return RelayMappingView(
        code = item.code,
        label = truncate(item.label, 80),
        rate = "%.2f".format(item.rate),
        stopCount = item.stopCount.toString(),
        revisionText = item.revision.toString(),
        loadKg = "%.2f".format(item.loadKg),
        trailerId = item.trailerId,
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayMappingViews(items: MutableList<RelayMapping>): MutableList<RelayMappingView> {
    val views = mutableListOf<RelayMappingView>()
    for (item in items) {
        views.add(toRelayMappingView(item))
    }
    return views
}

fun sumRelayMappingView(view: RelayMappingView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedRevision = view.revisionText.toInt()
    return parsedRate + parsedStopCount.toDouble() + parsedRevision.toDouble()
}
