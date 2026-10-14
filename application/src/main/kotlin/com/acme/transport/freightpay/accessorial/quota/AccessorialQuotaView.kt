package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial quota. */
data class AccessorialQuotaView(
    var code: String,
    var title: String,
    var segmentText: String,
    var driverIdText: String,
    var distanceKm: String,
    var summaryText: String,
    var archived: String,
    var amountText: String,
    var expiresAtText: String,
    var notesText: String,
    var statusLabel: String,
)

fun toAccessorialQuotaView(item: AccessorialQuota): AccessorialQuotaView {
    return AccessorialQuotaView(
        code = item.code,
        title = item.title,
        segmentText = truncate(item.segment, 40),
        driverIdText = item.driverId,
        distanceKm = "%.2f".format(item.distanceKm),
        summaryText = item.summary,
        archived = "${item.archived}",
        amountText = "%.2f".format(item.amount),
        expiresAtText = item.expiresAt.toString(),
        notesText = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialQuotaViews(items: MutableList<AccessorialQuota>): MutableList<AccessorialQuotaView> {
    val views = mutableListOf<AccessorialQuotaView>()
    for (item in items) {
        views.add(toAccessorialQuotaView(item))
    }
    return views
}

fun sumAccessorialQuotaView(view: AccessorialQuotaView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedAmount = view.amountText.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedDistanceKm + parsedAmount + parsedExpiresAt.toDouble()
}
