package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop allocation. */
data class DropAllocationView(
    var code: String,
    var name: String,
    var segment: String,
    var retriesText: String,
    var etaMinutesText: String,
    var shiftCode: String,
    var titleText: String,
    var fuelLiters: String,
    var channel: String,
    var statusLabel: String,
)

fun toDropAllocationView(item: DropAllocation): DropAllocationView {
    return DropAllocationView(
        code = truncate(item.code, 40),
        name = item.name,
        segment = item.segment,
        retriesText = item.retries.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        shiftCode = item.shiftCode,
        titleText = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropAllocationViews(items: MutableList<DropAllocation>): MutableList<DropAllocationView> {
    val views = mutableListOf<DropAllocationView>()
    for (item in items) {
        views.add(toDropAllocationView(item))
    }
    return views
}

fun sumDropAllocationView(view: DropAllocationView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedRetries.toDouble() + parsedEtaMinutes.toDouble() + parsedFuelLiters
}
