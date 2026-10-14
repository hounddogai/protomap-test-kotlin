package com.acme.transport.linehaul.relay.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay bundle. */
data class RelayBundleView(
    var code: String,
    var name: String,
    var contactEmail: String,
    var phone: String,
    var sequence: String,
    var createdAt: String,
    var dock: String,
    var ownerId: String,
    var driverId: String,
    var pallets: String,
    var statusLabel: String,
)

fun toRelayBundleView(item: RelayBundle): RelayBundleView {
    return RelayBundleView(
        code = item.code,
        name = item.name,
        contactEmail = item.contactEmail,
        phone = truncate(item.phone, 120),
        sequence = item.sequence.toString(),
        createdAt = item.createdAt.toString(),
        dock = item.dock,
        ownerId = truncate(item.ownerId, 120),
        driverId = item.driverId,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayBundleViews(items: MutableList<RelayBundle>): MutableList<RelayBundleView> {
    val views = mutableListOf<RelayBundleView>()
    for (item in items) {
        views.add(toRelayBundleView(item))
    }
    return views
}

fun sumRelayBundleView(view: RelayBundleView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedSequence.toDouble() + parsedCreatedAt.toDouble() + parsedPallets.toDouble()
}
