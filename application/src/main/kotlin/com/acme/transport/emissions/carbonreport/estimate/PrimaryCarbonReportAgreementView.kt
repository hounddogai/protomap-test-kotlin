package com.acme.transport.emissions.carbonreport.estimate

/** How screens and exports show a primary carbon report agreement. */
data class PrimaryCarbonReportAgreementView(
    var code: String,
    var labelText: String,
    var ratio: String,
    var currency: String,
    var odometer: String,
    var depot: String,
    var floorValue: String,
    var description: String,
    var graceDays: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toPrimaryCarbonReportAgreementView(item: PrimaryCarbonReportAgreement): PrimaryCarbonReportAgreementView {
    return PrimaryCarbonReportAgreementView(
        code = item.code,
        labelText = item.label,
        ratio = "%.2f".format(item.ratio),
        currency = item.currency,
        odometer = item.odometer.toString(),
        depot = item.depot,
        floorValue = "%.2f".format(item.floorValue),
        description = item.description,
        graceDays = item.graceDays.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCarbonReportAgreementViews(items: MutableList<PrimaryCarbonReportAgreement>): MutableList<PrimaryCarbonReportAgreementView> {
    val views = mutableListOf<PrimaryCarbonReportAgreementView>()
    for (item in items) {
        views.add(toPrimaryCarbonReportAgreementView(item))
    }
    return views
}

fun sumPrimaryCarbonReportAgreementView(view: PrimaryCarbonReportAgreementView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedRatio + parsedOdometer.toDouble() + parsedFloorValue
}
