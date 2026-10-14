package com.acme.transport.customs.clearance.profile

/** How screens and exports show a primary clearance profile. */
data class PrimaryClearanceProfileView(
    var code: String,
    var nameText: String,
    var etaMinutesText: String,
    var fuelLitersText: String,
    var externalRef: String,
    var rateText: String,
    var marginText: String,
    var description: String,
    var quantityText: String,
    var stopCount: String,
    var version: String,
    var statusLabel: String,
)

fun toPrimaryClearanceProfileView(item: PrimaryClearanceProfile): PrimaryClearanceProfileView {
    return PrimaryClearanceProfileView(
        code = item.code,
        nameText = item.name,
        etaMinutesText = item.etaMinutes.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        externalRef = item.externalRef,
        rateText = "%.2f".format(item.rate),
        marginText = "%.2f".format(item.margin),
        description = item.description,
        quantityText = item.quantity.toString(),
        stopCount = item.stopCount.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryClearanceProfileViews(items: MutableList<PrimaryClearanceProfile>): MutableList<PrimaryClearanceProfileView> {
    val views = mutableListOf<PrimaryClearanceProfileView>()
    for (item in items) {
        views.add(toPrimaryClearanceProfileView(item))
    }
    return views
}

fun sumPrimaryClearanceProfileView(view: PrimaryClearanceProfileView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedFuelLiters + parsedRate
}
