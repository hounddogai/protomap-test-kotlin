package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker notice. */
data class LockerNoticeView(
    var code: String,
    var nameText: String,
    var cost: String,
    var description: String,
    var maximum: String,
    var discount: String,
    var source: String,
    var vehicleId: String,
    var categoryText: String,
    var externalRef: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toLockerNoticeView(item: LockerNotice): LockerNoticeView {
    return LockerNoticeView(
        code = truncate(item.code, 64),
        nameText = item.name,
        cost = "%.2f".format(item.cost),
        description = item.description,
        maximum = "%.2f".format(item.maximum),
        discount = "%.2f".format(item.discount),
        source = truncate(item.source, 64),
        vehicleId = item.vehicleId,
        categoryText = item.category,
        externalRef = truncate(item.externalRef, 120),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerNoticeViews(items: MutableList<LockerNotice>): MutableList<LockerNoticeView> {
    val views = mutableListOf<LockerNoticeView>()
    for (item in items) {
        views.add(toLockerNoticeView(item))
    }
    return views
}

fun sumLockerNoticeView(view: LockerNoticeView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedCost + parsedMaximum + parsedDiscount
}
