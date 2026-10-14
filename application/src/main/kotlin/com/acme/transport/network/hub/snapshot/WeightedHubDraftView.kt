package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted hub draft. */
data class WeightedHubDraftView(
    var codeText: String,
    var labelText: String,
    var title: String,
    var version: String,
    var trailerId: String,
    var margin: String,
    var archived: String,
    var checksum: String,
    var statusLabel: String,
)

fun toWeightedHubDraftView(item: WeightedHubDraft): WeightedHubDraftView {
    return WeightedHubDraftView(
        codeText = item.code,
        labelText = item.label,
        title = item.title,
        version = item.version.toString(),
        trailerId = truncate(item.trailerId, 80),
        margin = "%.2f".format(item.margin),
        archived = "${item.archived}",
        checksum = truncate(item.checksum, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedHubDraftViews(items: MutableList<WeightedHubDraft>): MutableList<WeightedHubDraftView> {
    val views = mutableListOf<WeightedHubDraftView>()
    for (item in items) {
        views.add(toWeightedHubDraftView(item))
    }
    return views
}

fun sumWeightedHubDraftView(view: WeightedHubDraftView): Double {
    val parsedVersion = view.version.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedVersion.toDouble() + parsedMargin
}
