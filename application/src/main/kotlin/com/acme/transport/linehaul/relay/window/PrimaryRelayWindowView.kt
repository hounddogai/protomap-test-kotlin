package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary relay window. */
data class PrimaryRelayWindowView(
    var code: String,
    var name: String,
    var versionText: String,
    var updatedAt: String,
    var batchSize: String,
    var regionText: String,
    var statusLabel: String,
)

fun toPrimaryRelayWindowView(item: PrimaryRelayWindow): PrimaryRelayWindowView {
    return PrimaryRelayWindowView(
        code = truncate(item.code, 40),
        name = item.name,
        versionText = item.version.toString(),
        updatedAt = item.updatedAt.toString(),
        batchSize = item.batchSize.toString(),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRelayWindowViews(items: MutableList<PrimaryRelayWindow>): MutableList<PrimaryRelayWindowView> {
    val views = mutableListOf<PrimaryRelayWindowView>()
    for (item in items) {
        views.add(toPrimaryRelayWindowView(item))
    }
    return views
}

fun sumPrimaryRelayWindowView(view: PrimaryRelayWindowView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedVersion.toDouble() + parsedUpdatedAt.toDouble() + parsedBatchSize.toDouble()
}
