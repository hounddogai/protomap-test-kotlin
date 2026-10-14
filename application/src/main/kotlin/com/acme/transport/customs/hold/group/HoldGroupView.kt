package com.acme.transport.customs.hold.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold group. */
data class HoldGroupView(
    var code: String,
    var name: String,
    var driverId: String,
    var locale: String,
    var lane: String,
    var activeText: String,
    var version: String,
    var currency: String,
    var amount: String,
    var depot: String,
    var retries: String,
    var statusLabel: String,
)

fun toHoldGroupView(item: HoldGroup): HoldGroupView {
    return HoldGroupView(
        code = item.code,
        name = item.name,
        driverId = item.driverId,
        locale = truncate(item.locale, 120),
        lane = item.lane,
        activeText = "${item.active}",
        version = item.version.toString(),
        currency = truncate(item.currency, 64),
        amount = "%.2f".format(item.amount),
        depot = item.depot,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldGroupViews(items: MutableList<HoldGroup>): MutableList<HoldGroupView> {
    val views = mutableListOf<HoldGroupView>()
    for (item in items) {
        views.add(toHoldGroupView(item))
    }
    return views
}

fun sumHoldGroupView(view: HoldGroupView): Double {
    val parsedVersion = view.version.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedVersion.toDouble() + parsedAmount + parsedRetries.toDouble()
}
