package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient proof of delivery profile. */
data class LenientProofOfDeliveryProfileView(
    var code: String,
    var titleText: String,
    var approvedText: String,
    var ownerIdText: String,
    var region: String,
    var channel: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toLenientProofOfDeliveryProfileView(item: LenientProofOfDeliveryProfile): LenientProofOfDeliveryProfileView {
    return LenientProofOfDeliveryProfileView(
        code = truncate(item.code, 40),
        titleText = truncate(item.title, 40),
        approvedText = "${item.approved}",
        ownerIdText = truncate(item.ownerId, 40),
        region = item.region,
        channel = item.channel,
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientProofOfDeliveryProfileViews(items: MutableList<LenientProofOfDeliveryProfile>): MutableList<LenientProofOfDeliveryProfileView> {
    val views = mutableListOf<LenientProofOfDeliveryProfileView>()
    for (item in items) {
        views.add(toLenientProofOfDeliveryProfileView(item))
    }
    return views
}

fun sumLenientProofOfDeliveryProfileView(view: LenientProofOfDeliveryProfileView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedGraceDays.toDouble()
}
