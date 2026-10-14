package com.acme.transport.lastmile.locker.claim

/** How screens and exports show a pending locker claim. */
data class PendingLockerClaimView(
    var codeText: String,
    var title: String,
    var fuelLiters: String,
    var pinned: String,
    var carrierText: String,
    var channel: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toPendingLockerClaimView(item: PendingLockerClaim): PendingLockerClaimView {
    return PendingLockerClaimView(
        codeText = item.code,
        title = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        pinned = "${item.pinned}",
        carrierText = item.carrier,
        channel = item.channel,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingLockerClaimViews(items: MutableList<PendingLockerClaim>): MutableList<PendingLockerClaimView> {
    val views = mutableListOf<PendingLockerClaimView>()
    for (item in items) {
        views.add(toPendingLockerClaimView(item))
    }
    return views
}

fun sumPendingLockerClaimView(view: PendingLockerClaimView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedFuelLiters + parsedUpdatedAt.toDouble()
}
