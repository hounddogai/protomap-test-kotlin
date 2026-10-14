package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card manifest. */
data class RateCardManifestView(
    var code: String,
    var nameText: String,
    var amount: String,
    var expiresAt: String,
    var revision: String,
    var startsAt: String,
    var active: String,
    var carrier: String,
    var rate: String,
    var priority: String,
    var unit: String,
    var statusLabel: String,
)

fun toRateCardManifestView(item: RateCardManifest): RateCardManifestView {
    return RateCardManifestView(
        code = truncate(item.code, 40),
        nameText = item.name,
        amount = "%.2f".format(item.amount),
        expiresAt = item.expiresAt.toString(),
        revision = item.revision.toString(),
        startsAt = item.startsAt.toString(),
        active = "${item.active}",
        carrier = item.carrier,
        rate = "%.2f".format(item.rate),
        priority = item.priority.toString(),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardManifestViews(items: MutableList<RateCardManifest>): MutableList<RateCardManifestView> {
    val views = mutableListOf<RateCardManifestView>()
    for (item in items) {
        views.add(toRateCardManifestView(item))
    }
    return views
}

fun sumRateCardManifestView(view: RateCardManifestView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedAmount + parsedExpiresAt.toDouble() + parsedRevision.toDouble()
}
