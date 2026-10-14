package com.acme.transport.emissions.fuelburn.group

/** How screens and exports show a fuel burn quota. */
data class FuelBurnQuotaView(
    var codeText: String,
    var titleText: String,
    var carrier: String,
    var active: String,
    var capacity: String,
    var attemptsText: String,
    var startsAtText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toFuelBurnQuotaView(item: FuelBurnQuota): FuelBurnQuotaView {
    return FuelBurnQuotaView(
        codeText = item.code,
        titleText = item.title,
        carrier = item.carrier,
        active = "${item.active}",
        capacity = item.capacity.toString(),
        attemptsText = item.attempts.toString(),
        startsAtText = item.startsAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnQuotaViews(items: MutableList<FuelBurnQuota>): MutableList<FuelBurnQuotaView> {
    val views = mutableListOf<FuelBurnQuotaView>()
    for (item in items) {
        views.add(toFuelBurnQuotaView(item))
    }
    return views
}

fun sumFuelBurnQuotaView(view: FuelBurnQuotaView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedCapacity.toDouble() + parsedAttempts.toDouble() + parsedStartsAt.toDouble()
}
