package com.acme.transport.crewing.dutyperiod.snapshot

/** How screens and exports show a duty period profile. */
data class DutyPeriodProfileView(
    var codeText: String,
    var label: String,
    var threshold: String,
    var countryText: String,
    var approved: String,
    var odometerText: String,
    var rate: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDutyPeriodProfileView(item: DutyPeriodProfile): DutyPeriodProfileView {
    return DutyPeriodProfileView(
        codeText = item.code,
        label = item.label,
        threshold = "%.2f".format(item.threshold),
        countryText = item.country,
        approved = "${item.approved}",
        odometerText = item.odometer.toString(),
        rate = "%.2f".format(item.rate),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodProfileViews(items: MutableList<DutyPeriodProfile>): MutableList<DutyPeriodProfileView> {
    val views = mutableListOf<DutyPeriodProfileView>()
    for (item in items) {
        views.add(toDutyPeriodProfileView(item))
    }
    return views
}

fun sumDutyPeriodProfileView(view: DutyPeriodProfileView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedThreshold + parsedOdometer.toDouble() + parsedRate
}
