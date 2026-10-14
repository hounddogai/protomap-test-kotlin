package com.acme.transport.lastmile.locker.tier

/** How screens and exports show a tiered locker agreement. */
data class TieredLockerAgreementView(
    var codeText: String,
    var titleText: String,
    var odometerText: String,
    var fuelLiters: String,
    var createdAt: String,
    var thresholdText: String,
    var region: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toTieredLockerAgreementView(item: TieredLockerAgreement): TieredLockerAgreementView {
    return TieredLockerAgreementView(
        codeText = item.code,
        titleText = item.title,
        odometerText = item.odometer.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        createdAt = item.createdAt.toString(),
        thresholdText = "%.2f".format(item.threshold),
        region = item.region,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredLockerAgreementViews(items: MutableList<TieredLockerAgreement>): MutableList<TieredLockerAgreementView> {
    val views = mutableListOf<TieredLockerAgreementView>()
    for (item in items) {
        views.add(toTieredLockerAgreementView(item))
    }
    return views
}

fun sumTieredLockerAgreementView(view: TieredLockerAgreementView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedOdometer.toDouble() + parsedFuelLiters + parsedCreatedAt.toDouble()
}
