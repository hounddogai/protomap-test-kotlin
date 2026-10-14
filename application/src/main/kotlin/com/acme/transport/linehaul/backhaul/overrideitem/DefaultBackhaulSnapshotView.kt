package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default backhaul snapshot. */
data class DefaultBackhaulSnapshotView(
    var codeText: String,
    var title: String,
    var currency: String,
    var createdAt: String,
    var unit: String,
    var pallets: String,
    var floorValueText: String,
    var limit: String,
    var retries: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toDefaultBackhaulSnapshotView(item: DefaultBackhaulSnapshot): DefaultBackhaulSnapshotView {
    return DefaultBackhaulSnapshotView(
        codeText = item.code,
        title = item.title,
        currency = truncate(item.currency, 64),
        createdAt = item.createdAt.toString(),
        unit = item.unit,
        pallets = item.pallets.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        limit = item.limit.toString(),
        retries = item.retries.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultBackhaulSnapshotViews(items: MutableList<DefaultBackhaulSnapshot>): MutableList<DefaultBackhaulSnapshotView> {
    val views = mutableListOf<DefaultBackhaulSnapshotView>()
    for (item in items) {
        views.add(toDefaultBackhaulSnapshotView(item))
    }
    return views
}

fun sumDefaultBackhaulSnapshotView(view: DefaultBackhaulSnapshotView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedCreatedAt.toDouble() + parsedPallets.toDouble() + parsedFloorValue
}
