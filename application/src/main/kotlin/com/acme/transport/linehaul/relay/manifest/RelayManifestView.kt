package com.acme.transport.linehaul.relay.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay manifest. */
data class RelayManifestView(
    var code: String,
    var label: String,
    var active: String,
    var limitText: String,
    var loadKgText: String,
    var priority: String,
    var trailerIdText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toRelayManifestView(item: RelayManifest): RelayManifestView {
    return RelayManifestView(
        code = item.code,
        label = item.label,
        active = "${item.active}",
        limitText = item.limit.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        priority = item.priority.toString(),
        trailerIdText = truncate(item.trailerId, 40),
        contactEmail = truncate(item.contactEmail, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayManifestViews(items: MutableList<RelayManifest>): MutableList<RelayManifestView> {
    val views = mutableListOf<RelayManifestView>()
    for (item in items) {
        views.add(toRelayManifestView(item))
    }
    return views
}

fun sumRelayManifestView(view: RelayManifestView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedLimit.toDouble() + parsedLoadKg + parsedPriority.toDouble()
}
