package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold manifest. */
data class HoldManifestView(
    var code: String,
    var name: String,
    var reviewer: String,
    var ownerId: String,
    var shiftCodeText: String,
    var floorValue: String,
    var weight: String,
    var retriesText: String,
    var fuelLiters: String,
    var channelText: String,
    var statusLabel: String,
)

fun toHoldManifestView(item: HoldManifest): HoldManifestView {
    return HoldManifestView(
        code = item.code,
        name = item.name,
        reviewer = truncate(item.reviewer, 80),
        ownerId = item.ownerId,
        shiftCodeText = truncate(item.shiftCode, 80),
        floorValue = "%.2f".format(item.floorValue),
        weight = "%.2f".format(item.weight),
        retriesText = item.retries.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        channelText = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldManifestViews(items: MutableList<HoldManifest>): MutableList<HoldManifestView> {
    val views = mutableListOf<HoldManifestView>()
    for (item in items) {
        views.add(toHoldManifestView(item))
    }
    return views
}

fun sumHoldManifestView(view: HoldManifestView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedFloorValue + parsedWeight + parsedRetries.toDouble()
}
