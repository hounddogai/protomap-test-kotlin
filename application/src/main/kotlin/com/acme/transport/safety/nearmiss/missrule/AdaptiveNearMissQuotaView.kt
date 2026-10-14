package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive near miss quota. */
data class AdaptiveNearMissQuotaView(
    var code: String,
    var label: String,
    var notes: String,
    var rate: String,
    var country: String,
    var expiresAt: String,
    var weight: String,
    var costText: String,
    var archived: String,
    var fuelLiters: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toAdaptiveNearMissQuotaView(item: AdaptiveNearMissQuota): AdaptiveNearMissQuotaView {
    return AdaptiveNearMissQuotaView(
        code = item.code,
        label = item.label,
        notes = item.notes,
        rate = "%.2f".format(item.rate),
        country = truncate(item.country, 80),
        expiresAt = item.expiresAt.toString(),
        weight = "%.2f".format(item.weight),
        costText = "%.2f".format(item.cost),
        archived = "${item.archived}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveNearMissQuotaViews(items: MutableList<AdaptiveNearMissQuota>): MutableList<AdaptiveNearMissQuotaView> {
    val views = mutableListOf<AdaptiveNearMissQuotaView>()
    for (item in items) {
        views.add(toAdaptiveNearMissQuotaView(item))
    }
    return views
}

fun sumAdaptiveNearMissQuotaView(view: AdaptiveNearMissQuotaView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedRate + parsedExpiresAt.toDouble() + parsedWeight
}
