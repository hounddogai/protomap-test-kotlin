package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient rate card policy. */
data class LenientRateCardPolicyView(
    var codeText: String,
    var name: String,
    var channel: String,
    var floorValue: String,
    var pallets: String,
    var revision: String,
    var capacity: String,
    var segment: String,
    var discountText: String,
    var statusLabel: String,
)

fun toLenientRateCardPolicyView(item: LenientRateCardPolicy): LenientRateCardPolicyView {
    return LenientRateCardPolicyView(
        codeText = truncate(item.code, 64),
        name = item.name,
        channel = item.channel,
        floorValue = "%.2f".format(item.floorValue),
        pallets = item.pallets.toString(),
        revision = item.revision.toString(),
        capacity = item.capacity.toString(),
        segment = item.segment,
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRateCardPolicyViews(items: MutableList<LenientRateCardPolicy>): MutableList<LenientRateCardPolicyView> {
    val views = mutableListOf<LenientRateCardPolicyView>()
    for (item in items) {
        views.add(toLenientRateCardPolicyView(item))
    }
    return views
}

fun sumLenientRateCardPolicyView(view: LenientRateCardPolicyView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedFloorValue + parsedPallets.toDouble() + parsedRevision.toDouble()
}
