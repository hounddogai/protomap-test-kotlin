package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary proof of delivery notice. */
data class PrimaryProofOfDeliveryNoticeView(
    var code: String,
    var name: String,
    var discount: String,
    var maximum: String,
    var capacityText: String,
    var limit: String,
    var ratio: String,
    var priority: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toPrimaryProofOfDeliveryNoticeView(item: PrimaryProofOfDeliveryNotice): PrimaryProofOfDeliveryNoticeView {
    return PrimaryProofOfDeliveryNoticeView(
        code = item.code,
        name = truncate(item.name, 120),
        discount = "%.2f".format(item.discount),
        maximum = "%.2f".format(item.maximum),
        capacityText = item.capacity.toString(),
        limit = item.limit.toString(),
        ratio = "%.2f".format(item.ratio),
        priority = item.priority.toString(),
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryProofOfDeliveryNoticeViews(items: MutableList<PrimaryProofOfDeliveryNotice>): MutableList<PrimaryProofOfDeliveryNoticeView> {
    val views = mutableListOf<PrimaryProofOfDeliveryNoticeView>()
    for (item in items) {
        views.add(toPrimaryProofOfDeliveryNoticeView(item))
    }
    return views
}

fun sumPrimaryProofOfDeliveryNoticeView(view: PrimaryProofOfDeliveryNoticeView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedCapacity = view.capacityText.toInt()
    return parsedDiscount + parsedMaximum + parsedCapacity.toDouble()
}
