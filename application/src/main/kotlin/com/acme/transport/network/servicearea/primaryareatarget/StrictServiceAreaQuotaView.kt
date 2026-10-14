package com.acme.transport.network.servicearea.primaryareatarget

/** How screens and exports show a strict service area quota. */
data class StrictServiceAreaQuotaView(
    var code: String,
    var name: String,
    var attemptsText: String,
    var contactEmail: String,
    var distanceKm: String,
    var windowDays: String,
    var budget: String,
    var expiresAt: String,
    var pinned: String,
    var statusLabel: String,
)

fun toStrictServiceAreaQuotaView(item: StrictServiceAreaQuota): StrictServiceAreaQuotaView {
    return StrictServiceAreaQuotaView(
        code = item.code,
        name = item.name,
        attemptsText = item.attempts.toString(),
        contactEmail = item.contactEmail,
        distanceKm = "%.2f".format(item.distanceKm),
        windowDays = item.windowDays.toString(),
        budget = "%.2f".format(item.budget),
        expiresAt = item.expiresAt.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictServiceAreaQuotaViews(items: MutableList<StrictServiceAreaQuota>): MutableList<StrictServiceAreaQuotaView> {
    val views = mutableListOf<StrictServiceAreaQuotaView>()
    for (item in items) {
        views.add(toStrictServiceAreaQuotaView(item))
    }
    return views
}

fun sumStrictServiceAreaQuotaView(view: StrictServiceAreaQuotaView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedAttempts.toDouble() + parsedDistanceKm + parsedWindowDays.toDouble()
}
