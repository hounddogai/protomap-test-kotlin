package com.acme.transport.network.hub.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hub notice. */
data class HubNoticeView(
    var codeText: String,
    var name: String,
    var source: String,
    var attempts: String,
    var trailerId: String,
    var amountText: String,
    var createdAt: String,
    var fuelLiters: String,
    var maximum: String,
    var odometer: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toHubNoticeView(item: HubNotice): HubNoticeView {
    return HubNoticeView(
        codeText = item.code,
        name = truncate(item.name, 120),
        source = item.source,
        attempts = item.attempts.toString(),
        trailerId = item.trailerId,
        amountText = "%.2f".format(item.amount),
        createdAt = item.createdAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        maximum = "%.2f".format(item.maximum),
        odometer = item.odometer.toString(),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubNoticeViews(items: MutableList<HubNotice>): MutableList<HubNoticeView> {
    val views = mutableListOf<HubNoticeView>()
    for (item in items) {
        views.add(toHubNoticeView(item))
    }
    return views
}

fun sumHubNoticeView(view: HubNoticeView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedAttempts.toDouble() + parsedAmount + parsedCreatedAt.toDouble()
}
