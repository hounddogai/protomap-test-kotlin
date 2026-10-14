package com.acme.transport.emissions.fuelburn.target

/** How screens and exports show a fuel burn target. */
data class FuelBurnTargetView(
    var codeText: String,
    var name: String,
    var channelText: String,
    var checksum: String,
    var weight: String,
    var statusLabel: String,
)

fun toFuelBurnTargetView(item: FuelBurnTarget): FuelBurnTargetView {
    return FuelBurnTargetView(
        codeText = item.code,
        name = item.name,
        channelText = item.channel,
        checksum = item.checksum,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnTargetViews(items: MutableList<FuelBurnTarget>): MutableList<FuelBurnTargetView> {
    val views = mutableListOf<FuelBurnTargetView>()
    for (item in items) {
        views.add(toFuelBurnTargetView(item))
    }
    return views
}

fun sumFuelBurnTargetView(view: FuelBurnTargetView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
