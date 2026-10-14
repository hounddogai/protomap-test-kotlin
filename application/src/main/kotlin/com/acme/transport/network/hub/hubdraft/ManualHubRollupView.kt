package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual hub rollup. */
data class ManualHubRollupView(
    var code: String,
    var name: String,
    var graceDays: String,
    var floorValue: String,
    var retries: String,
    var limit: String,
    var quantity: String,
    var endsAt: String,
    var shiftCode: String,
    var amount: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toManualHubRollupView(item: ManualHubRollup): ManualHubRollupView {
    return ManualHubRollupView(
        code = item.code,
        name = item.name,
        graceDays = item.graceDays.toString(),
        floorValue = "%.2f".format(item.floorValue),
        retries = item.retries.toString(),
        limit = item.limit.toString(),
        quantity = item.quantity.toString(),
        endsAt = item.endsAt.toString(),
        shiftCode = truncate(item.shiftCode, 120),
        amount = "%.2f".format(item.amount),
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualHubRollupViews(items: MutableList<ManualHubRollup>): MutableList<ManualHubRollupView> {
    val views = mutableListOf<ManualHubRollupView>()
    for (item in items) {
        views.add(toManualHubRollupView(item))
    }
    return views
}

fun sumManualHubRollupView(view: ManualHubRollupView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedGraceDays.toDouble() + parsedFloorValue + parsedRetries.toDouble()
}
