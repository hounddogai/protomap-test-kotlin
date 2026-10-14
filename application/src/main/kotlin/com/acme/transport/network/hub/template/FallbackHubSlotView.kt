package com.acme.transport.network.hub.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback hub slot. */
data class FallbackHubSlotView(
    var code: String,
    var name: String,
    var createdAt: String,
    var palletsText: String,
    var retries: String,
    var notes: String,
    var version: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toFallbackHubSlotView(item: FallbackHubSlot): FallbackHubSlotView {
    return FallbackHubSlotView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 120),
        createdAt = item.createdAt.toString(),
        palletsText = item.pallets.toString(),
        retries = item.retries.toString(),
        notes = truncate(item.notes, 40),
        version = item.version.toString(),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackHubSlotViews(items: MutableList<FallbackHubSlot>): MutableList<FallbackHubSlotView> {
    val views = mutableListOf<FallbackHubSlotView>()
    for (item in items) {
        views.add(toFallbackHubSlotView(item))
    }
    return views
}

fun sumFallbackHubSlotView(view: FallbackHubSlotView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedCreatedAt.toDouble() + parsedPallets.toDouble() + parsedRetries.toDouble()
}
