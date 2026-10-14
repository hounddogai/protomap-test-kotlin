package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered proof of delivery manifest. */
data class TieredProofOfDeliveryManifestView(
    var codeText: String,
    var nameText: String,
    var shiftCode: String,
    var currency: String,
    var durationMinutes: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toTieredProofOfDeliveryManifestView(item: TieredProofOfDeliveryManifest): TieredProofOfDeliveryManifestView {
    return TieredProofOfDeliveryManifestView(
        codeText = truncate(item.code, 120),
        nameText = item.name,
        shiftCode = item.shiftCode,
        currency = item.currency,
        durationMinutes = item.durationMinutes.toString(),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredProofOfDeliveryManifestViews(items: MutableList<TieredProofOfDeliveryManifest>): MutableList<TieredProofOfDeliveryManifestView> {
    val views = mutableListOf<TieredProofOfDeliveryManifestView>()
    for (item in items) {
        views.add(toTieredProofOfDeliveryManifestView(item))
    }
    return views
}

fun sumTieredProofOfDeliveryManifestView(view: TieredProofOfDeliveryManifestView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble()
}
