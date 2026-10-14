package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive broker batch. */
data class AdaptiveBrokerBatchView(
    var code: String,
    var title: String,
    var archived: String,
    var approved: String,
    var source: String,
    var phone: String,
    var shiftCode: String,
    var startsAt: String,
    var carrier: String,
    var pallets: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toAdaptiveBrokerBatchView(item: AdaptiveBrokerBatch): AdaptiveBrokerBatchView {
    return AdaptiveBrokerBatchView(
        code = item.code,
        title = item.title,
        archived = "${item.archived}",
        approved = "${item.approved}",
        source = item.source,
        phone = item.phone,
        shiftCode = truncate(item.shiftCode, 120),
        startsAt = item.startsAt.toString(),
        carrier = item.carrier,
        pallets = item.pallets.toString(),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveBrokerBatchViews(items: MutableList<AdaptiveBrokerBatch>): MutableList<AdaptiveBrokerBatchView> {
    val views = mutableListOf<AdaptiveBrokerBatchView>()
    for (item in items) {
        views.add(toAdaptiveBrokerBatchView(item))
    }
    return views
}

fun sumAdaptiveBrokerBatchView(view: AdaptiveBrokerBatchView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedStartsAt.toDouble() + parsedPallets.toDouble() + parsedCreatedAt.toDouble()
}
