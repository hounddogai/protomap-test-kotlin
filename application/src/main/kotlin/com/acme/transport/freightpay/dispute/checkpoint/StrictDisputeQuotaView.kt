package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict dispute quota. */
data class StrictDisputeQuotaView(
    var code: String,
    var title: String,
    var maximum: String,
    var approvedText: String,
    var threshold: String,
    var ownerIdText: String,
    var statusLabel: String,
)

fun toStrictDisputeQuotaView(item: StrictDisputeQuota): StrictDisputeQuotaView {
    return StrictDisputeQuotaView(
        code = truncate(item.code, 120),
        title = item.title,
        maximum = "%.2f".format(item.maximum),
        approvedText = "${item.approved}",
        threshold = "%.2f".format(item.threshold),
        ownerIdText = truncate(item.ownerId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDisputeQuotaViews(items: MutableList<StrictDisputeQuota>): MutableList<StrictDisputeQuotaView> {
    val views = mutableListOf<StrictDisputeQuotaView>()
    for (item in items) {
        views.add(toStrictDisputeQuotaView(item))
    }
    return views
}

fun sumStrictDisputeQuotaView(view: StrictDisputeQuotaView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedMaximum + parsedThreshold
}
