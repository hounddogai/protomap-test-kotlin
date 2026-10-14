package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global proof of delivery snapshot. */
data class GlobalProofOfDeliverySnapshotView(
    var code: String,
    var label: String,
    var loadKg: String,
    var durationMinutesText: String,
    var cost: String,
    var windowDaysText: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toGlobalProofOfDeliverySnapshotView(item: GlobalProofOfDeliverySnapshot): GlobalProofOfDeliverySnapshotView {
    return GlobalProofOfDeliverySnapshotView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 40),
        loadKg = "%.2f".format(item.loadKg),
        durationMinutesText = item.durationMinutes.toString(),
        cost = "%.2f".format(item.cost),
        windowDaysText = item.windowDays.toString(),
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalProofOfDeliverySnapshotViews(items: MutableList<GlobalProofOfDeliverySnapshot>): MutableList<GlobalProofOfDeliverySnapshotView> {
    val views = mutableListOf<GlobalProofOfDeliverySnapshotView>()
    for (item in items) {
        views.add(toGlobalProofOfDeliverySnapshotView(item))
    }
    return views
}

fun sumGlobalProofOfDeliverySnapshotView(view: GlobalProofOfDeliverySnapshotView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedLoadKg + parsedDurationMinutes.toDouble() + parsedCost
}
