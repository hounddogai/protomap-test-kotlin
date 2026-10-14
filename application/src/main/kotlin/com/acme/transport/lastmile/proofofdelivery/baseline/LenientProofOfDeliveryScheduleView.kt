package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient proof of delivery schedule. */
data class LenientProofOfDeliveryScheduleView(
    var code: String,
    var label: String,
    var discountText: String,
    var timezoneText: String,
    var startsAt: String,
    var limit: String,
    var checksumText: String,
    var depot: String,
    var capacity: String,
    var maximum: String,
    var statusLabel: String,
)

fun toLenientProofOfDeliveryScheduleView(item: LenientProofOfDeliverySchedule): LenientProofOfDeliveryScheduleView {
    return LenientProofOfDeliveryScheduleView(
        code = item.code,
        label = truncate(item.label, 64),
        discountText = "%.2f".format(item.discount),
        timezoneText = item.timezone,
        startsAt = item.startsAt.toString(),
        limit = item.limit.toString(),
        checksumText = item.checksum,
        depot = item.depot,
        capacity = item.capacity.toString(),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientProofOfDeliveryScheduleViews(items: MutableList<LenientProofOfDeliverySchedule>): MutableList<LenientProofOfDeliveryScheduleView> {
    val views = mutableListOf<LenientProofOfDeliveryScheduleView>()
    for (item in items) {
        views.add(toLenientProofOfDeliveryScheduleView(item))
    }
    return views
}

fun sumLenientProofOfDeliveryScheduleView(view: LenientProofOfDeliveryScheduleView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedDiscount + parsedStartsAt.toDouble() + parsedLimit.toDouble()
}
