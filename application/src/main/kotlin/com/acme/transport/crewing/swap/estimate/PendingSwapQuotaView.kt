package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending swap quota. */
data class PendingSwapQuotaView(
    var code: String,
    var label: String,
    var contactEmail: String,
    var threshold: String,
    var fuelLiters: String,
    var ownerId: String,
    var discountText: String,
    var batchSize: String,
    var attempts: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toPendingSwapQuotaView(item: PendingSwapQuota): PendingSwapQuotaView {
    return PendingSwapQuotaView(
        code = truncate(item.code, 64),
        label = item.label,
        contactEmail = item.contactEmail,
        threshold = "%.2f".format(item.threshold),
        fuelLiters = "%.2f".format(item.fuelLiters),
        ownerId = truncate(item.ownerId, 40),
        discountText = "%.2f".format(item.discount),
        batchSize = item.batchSize.toString(),
        attempts = item.attempts.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingSwapQuotaViews(items: MutableList<PendingSwapQuota>): MutableList<PendingSwapQuotaView> {
    val views = mutableListOf<PendingSwapQuotaView>()
    for (item in items) {
        views.add(toPendingSwapQuotaView(item))
    }
    return views
}

fun sumPendingSwapQuotaView(view: PendingSwapQuotaView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedThreshold + parsedFuelLiters + parsedDiscount
}
