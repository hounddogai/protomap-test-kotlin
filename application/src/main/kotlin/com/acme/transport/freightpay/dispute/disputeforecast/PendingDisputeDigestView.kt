package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending dispute digest. */
data class PendingDisputeDigestView(
    var codeText: String,
    var title: String,
    var version: String,
    var reviewer: String,
    var graceDays: String,
    var depot: String,
    var unitText: String,
    var statusLabel: String,
)

fun toPendingDisputeDigestView(item: PendingDisputeDigest): PendingDisputeDigestView {
    return PendingDisputeDigestView(
        codeText = item.code,
        title = truncate(item.title, 120),
        version = item.version.toString(),
        reviewer = item.reviewer,
        graceDays = item.graceDays.toString(),
        depot = truncate(item.depot, 40),
        unitText = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDisputeDigestViews(items: MutableList<PendingDisputeDigest>): MutableList<PendingDisputeDigestView> {
    val views = mutableListOf<PendingDisputeDigestView>()
    for (item in items) {
        views.add(toPendingDisputeDigestView(item))
    }
    return views
}

fun sumPendingDisputeDigestView(view: PendingDisputeDigestView): Double {
    val parsedVersion = view.version.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedVersion.toDouble() + parsedGraceDays.toDouble()
}
