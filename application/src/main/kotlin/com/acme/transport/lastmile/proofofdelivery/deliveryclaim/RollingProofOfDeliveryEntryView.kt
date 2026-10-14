package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling proof of delivery entry. */
data class RollingProofOfDeliveryEntryView(
    var code: String,
    var titleText: String,
    var discountText: String,
    var shiftCode: String,
    var pallets: String,
    var externalRef: String,
    var priority: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toRollingProofOfDeliveryEntryView(item: RollingProofOfDeliveryEntry): RollingProofOfDeliveryEntryView {
    return RollingProofOfDeliveryEntryView(
        code = item.code,
        titleText = item.title,
        discountText = "%.2f".format(item.discount),
        shiftCode = truncate(item.shiftCode, 40),
        pallets = item.pallets.toString(),
        externalRef = item.externalRef,
        priority = item.priority.toString(),
        carrierText = truncate(item.carrier, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingProofOfDeliveryEntryViews(items: MutableList<RollingProofOfDeliveryEntry>): MutableList<RollingProofOfDeliveryEntryView> {
    val views = mutableListOf<RollingProofOfDeliveryEntryView>()
    for (item in items) {
        views.add(toRollingProofOfDeliveryEntryView(item))
    }
    return views
}

fun sumRollingProofOfDeliveryEntryView(view: RollingProofOfDeliveryEntryView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedDiscount + parsedPallets.toDouble() + parsedPriority.toDouble()
}
