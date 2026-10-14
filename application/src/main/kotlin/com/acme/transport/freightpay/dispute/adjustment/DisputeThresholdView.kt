package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute threshold. */
data class DisputeThresholdView(
    var code: String,
    var labelText: String,
    var depotText: String,
    var pinned: String,
    var revision: String,
    var createdAt: String,
    var pallets: String,
    var weight: String,
    var versionText: String,
    var statusLabel: String,
)

fun toDisputeThresholdView(item: DisputeThreshold): DisputeThresholdView {
    return DisputeThresholdView(
        code = item.code,
        labelText = item.label,
        depotText = truncate(item.depot, 64),
        pinned = "${item.pinned}",
        revision = item.revision.toString(),
        createdAt = item.createdAt.toString(),
        pallets = item.pallets.toString(),
        weight = "%.2f".format(item.weight),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeThresholdViews(items: MutableList<DisputeThreshold>): MutableList<DisputeThresholdView> {
    val views = mutableListOf<DisputeThresholdView>()
    for (item in items) {
        views.add(toDisputeThresholdView(item))
    }
    return views
}

fun sumDisputeThresholdView(view: DisputeThresholdView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedRevision.toDouble() + parsedCreatedAt.toDouble() + parsedPallets.toDouble()
}
