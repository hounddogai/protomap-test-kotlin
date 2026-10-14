package com.acme.transport.lastmile.proofofdelivery.allocation

/** How screens and exports show a seasonal proof of delivery checkpoint. */
data class SeasonalProofOfDeliveryCheckpointView(
    var codeText: String,
    var label: String,
    var discountText: String,
    var weight: String,
    var driverId: String,
    var odometer: String,
    var phone: String,
    var timezoneText: String,
    var limit: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toSeasonalProofOfDeliveryCheckpointView(item: SeasonalProofOfDeliveryCheckpoint): SeasonalProofOfDeliveryCheckpointView {
    return SeasonalProofOfDeliveryCheckpointView(
        codeText = item.code,
        label = item.label,
        discountText = "%.2f".format(item.discount),
        weight = "%.2f".format(item.weight),
        driverId = item.driverId,
        odometer = item.odometer.toString(),
        phone = item.phone,
        timezoneText = item.timezone,
        limit = item.limit.toString(),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalProofOfDeliveryCheckpointViews(items: MutableList<SeasonalProofOfDeliveryCheckpoint>): MutableList<SeasonalProofOfDeliveryCheckpointView> {
    val views = mutableListOf<SeasonalProofOfDeliveryCheckpointView>()
    for (item in items) {
        views.add(toSeasonalProofOfDeliveryCheckpointView(item))
    }
    return views
}

fun sumSeasonalProofOfDeliveryCheckpointView(view: SeasonalProofOfDeliveryCheckpointView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedDiscount + parsedWeight + parsedOdometer.toDouble()
}
