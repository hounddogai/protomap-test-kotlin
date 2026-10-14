package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy proof of delivery request. */
data class LegacyProofOfDeliveryRequestView(
    var codeText: String,
    var titleText: String,
    var etaMinutes: String,
    var timezone: String,
    var createdAtText: String,
    var updatedAt: String,
    var visible: String,
    var statusLabel: String,
)

fun toLegacyProofOfDeliveryRequestView(item: LegacyProofOfDeliveryRequest): LegacyProofOfDeliveryRequestView {
    return LegacyProofOfDeliveryRequestView(
        codeText = truncate(item.code, 40),
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        timezone = truncate(item.timezone, 64),
        createdAtText = item.createdAt.toString(),
        updatedAt = item.updatedAt.toString(),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyProofOfDeliveryRequestViews(items: MutableList<LegacyProofOfDeliveryRequest>): MutableList<LegacyProofOfDeliveryRequestView> {
    val views = mutableListOf<LegacyProofOfDeliveryRequestView>()
    for (item in items) {
        views.add(toLegacyProofOfDeliveryRequestView(item))
    }
    return views
}

fun sumLegacyProofOfDeliveryRequestView(view: LegacyProofOfDeliveryRequestView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedEtaMinutes.toDouble() + parsedCreatedAt.toDouble() + parsedUpdatedAt.toDouble()
}
