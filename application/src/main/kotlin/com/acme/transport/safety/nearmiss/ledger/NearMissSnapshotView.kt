package com.acme.transport.safety.nearmiss.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss snapshot. */
data class NearMissSnapshotView(
    var code: String,
    var title: String,
    var channelText: String,
    var pallets: String,
    var threshold: String,
    var quantityText: String,
    var pinned: String,
    var country: String,
    var budgetText: String,
    var costText: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toNearMissSnapshotView(item: NearMissSnapshot): NearMissSnapshotView {
    return NearMissSnapshotView(
        code = item.code,
        title = item.title,
        channelText = item.channel,
        pallets = item.pallets.toString(),
        threshold = "%.2f".format(item.threshold),
        quantityText = item.quantity.toString(),
        pinned = "${item.pinned}",
        country = truncate(item.country, 40),
        budgetText = "%.2f".format(item.budget),
        costText = "%.2f".format(item.cost),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissSnapshotViews(items: MutableList<NearMissSnapshot>): MutableList<NearMissSnapshotView> {
    val views = mutableListOf<NearMissSnapshotView>()
    for (item in items) {
        views.add(toNearMissSnapshotView(item))
    }
    return views
}

fun sumNearMissSnapshotView(view: NearMissSnapshotView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedPallets.toDouble() + parsedThreshold + parsedQuantity.toDouble()
}
