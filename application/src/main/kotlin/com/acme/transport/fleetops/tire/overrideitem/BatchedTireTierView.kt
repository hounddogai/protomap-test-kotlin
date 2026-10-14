package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched tire tier. */
data class BatchedTireTierView(
    var code: String,
    var name: String,
    var palletsText: String,
    var fuelLiters: String,
    var retries: String,
    var depotText: String,
    var phone: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toBatchedTireTierView(item: BatchedTireTier): BatchedTireTierView {
    return BatchedTireTierView(
        code = item.code,
        name = truncate(item.name, 40),
        palletsText = item.pallets.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        retries = item.retries.toString(),
        depotText = truncate(item.depot, 64),
        phone = truncate(item.phone, 40),
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTireTierViews(items: MutableList<BatchedTireTier>): MutableList<BatchedTireTierView> {
    val views = mutableListOf<BatchedTireTierView>()
    for (item in items) {
        views.add(toBatchedTireTierView(item))
    }
    return views
}

fun sumBatchedTireTierView(view: BatchedTireTierView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedPallets.toDouble() + parsedFuelLiters + parsedRetries.toDouble()
}
