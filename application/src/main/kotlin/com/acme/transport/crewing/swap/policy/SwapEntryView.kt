package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap entry. */
data class SwapEntryView(
    var code: String,
    var name: String,
    var discount: String,
    var cost: String,
    var checksumText: String,
    var shiftCode: String,
    var fuelLiters: String,
    var stopCount: String,
    var trailerId: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toSwapEntryView(item: SwapEntry): SwapEntryView {
    return SwapEntryView(
        code = item.code,
        name = item.name,
        discount = "%.2f".format(item.discount),
        cost = "%.2f".format(item.cost),
        checksumText = truncate(item.checksum, 40),
        shiftCode = item.shiftCode,
        fuelLiters = "%.2f".format(item.fuelLiters),
        stopCount = item.stopCount.toString(),
        trailerId = item.trailerId,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapEntryViews(items: MutableList<SwapEntry>): MutableList<SwapEntryView> {
    val views = mutableListOf<SwapEntryView>()
    for (item in items) {
        views.add(toSwapEntryView(item))
    }
    return views
}

fun sumSwapEntryView(view: SwapEntryView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedDiscount + parsedCost + parsedFuelLiters
}
