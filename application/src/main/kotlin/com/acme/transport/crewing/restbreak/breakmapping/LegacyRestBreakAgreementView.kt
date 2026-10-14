package com.acme.transport.crewing.restbreak.breakmapping

/** How screens and exports show a legacy rest break agreement. */
data class LegacyRestBreakAgreementView(
    var code: String,
    var label: String,
    var segment: String,
    var laneText: String,
    var vehicleIdText: String,
    var fuelLitersText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toLegacyRestBreakAgreementView(item: LegacyRestBreakAgreement): LegacyRestBreakAgreementView {
    return LegacyRestBreakAgreementView(
        code = item.code,
        label = item.label,
        segment = item.segment,
        laneText = item.lane,
        vehicleIdText = item.vehicleId,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyRestBreakAgreementViews(items: MutableList<LegacyRestBreakAgreement>): MutableList<LegacyRestBreakAgreementView> {
    val views = mutableListOf<LegacyRestBreakAgreementView>()
    for (item in items) {
        views.add(toLegacyRestBreakAgreementView(item))
    }
    return views
}

fun sumLegacyRestBreakAgreementView(view: LegacyRestBreakAgreementView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedFuelLiters
}
