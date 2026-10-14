package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery bundle. */
data class RedeliveryBundleView(
    var code: String,
    var name: String,
    var amount: String,
    var fuelLiters: String,
    var country: String,
    var lane: String,
    var checksumText: String,
    var routeCodeText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toRedeliveryBundleView(item: RedeliveryBundle): RedeliveryBundleView {
    return RedeliveryBundleView(
        code = item.code,
        name = truncate(item.name, 40),
        amount = "%.2f".format(item.amount),
        fuelLiters = "%.2f".format(item.fuelLiters),
        country = item.country,
        lane = item.lane,
        checksumText = truncate(item.checksum, 64),
        routeCodeText = item.routeCode,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryBundleViews(items: MutableList<RedeliveryBundle>): MutableList<RedeliveryBundleView> {
    val views = mutableListOf<RedeliveryBundleView>()
    for (item in items) {
        views.add(toRedeliveryBundleView(item))
    }
    return views
}

fun sumRedeliveryBundleView(view: RedeliveryBundleView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedAmount + parsedFuelLiters + parsedPallets.toDouble()
}
