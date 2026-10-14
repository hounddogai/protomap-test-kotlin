package com.acme.transport.network.hub.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub manifest. */
data class HubManifestView(
    var codeText: String,
    var labelText: String,
    var endsAtText: String,
    var segmentText: String,
    var categoryText: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toHubManifestView(item: HubManifest): HubManifestView {
    return HubManifestView(
        codeText = item.code,
        labelText = truncate(item.label, 40),
        endsAtText = item.endsAt.toString(),
        segmentText = truncate(item.segment, 40),
        categoryText = item.category,
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubManifestViews(items: MutableList<HubManifest>): MutableList<HubManifestView> {
    val views = mutableListOf<HubManifestView>()
    for (item in items) {
        views.add(toHubManifestView(item))
    }
    return views
}

fun sumHubManifestView(view: HubManifestView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedEndsAt.toDouble()
}
