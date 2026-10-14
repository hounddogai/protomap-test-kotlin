package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute manifest. */
data class DisputeManifestView(
    var codeText: String,
    var title: String,
    var stopCount: String,
    var attempts: String,
    var limit: String,
    var quantityText: String,
    var ownerId: String,
    var phone: String,
    var regionText: String,
    var statusLabel: String,
)

fun toDisputeManifestView(item: DisputeManifest): DisputeManifestView {
    return DisputeManifestView(
        codeText = item.code,
        title = item.title,
        stopCount = item.stopCount.toString(),
        attempts = item.attempts.toString(),
        limit = item.limit.toString(),
        quantityText = item.quantity.toString(),
        ownerId = item.ownerId,
        phone = truncate(item.phone, 40),
        regionText = truncate(item.region, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeManifestViews(items: MutableList<DisputeManifest>): MutableList<DisputeManifestView> {
    val views = mutableListOf<DisputeManifestView>()
    for (item in items) {
        views.add(toDisputeManifestView(item))
    }
    return views
}

fun sumDisputeManifestView(view: DisputeManifestView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedStopCount.toDouble() + parsedAttempts.toDouble() + parsedLimit.toDouble()
}
