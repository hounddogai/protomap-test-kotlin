package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub mapping. */
data class HubMappingView(
    var codeText: String,
    var name: String,
    var endsAt: String,
    var localeText: String,
    var trailerId: String,
    var maximum: String,
    var ratio: String,
    var statusLabel: String,
)

fun toHubMappingView(item: HubMapping): HubMappingView {
    return HubMappingView(
        codeText = truncate(item.code, 64),
        name = truncate(item.name, 64),
        endsAt = item.endsAt.toString(),
        localeText = item.locale,
        trailerId = truncate(item.trailerId, 120),
        maximum = "%.2f".format(item.maximum),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubMappingViews(items: MutableList<HubMapping>): MutableList<HubMappingView> {
    val views = mutableListOf<HubMappingView>()
    for (item in items) {
        views.add(toHubMappingView(item))
    }
    return views
}

fun sumHubMappingView(view: HubMappingView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedEndsAt.toDouble() + parsedMaximum + parsedRatio
}
