package com.acme.transport.emissions.carbonreport.reportmapping

/** How screens and exports show a strict carbon report mapping. */
data class StrictCarbonReportMappingView(
    var codeText: String,
    var label: String,
    var checksum: String,
    var notes: String,
    var endsAt: String,
    var driverId: String,
    var ceiling: String,
    var contactEmail: String,
    var currency: String,
    var statusLabel: String,
)

fun toStrictCarbonReportMappingView(item: StrictCarbonReportMapping): StrictCarbonReportMappingView {
    return StrictCarbonReportMappingView(
        codeText = item.code,
        label = item.label,
        checksum = item.checksum,
        notes = item.notes,
        endsAt = item.endsAt.toString(),
        driverId = item.driverId,
        ceiling = "%.2f".format(item.ceiling),
        contactEmail = item.contactEmail,
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCarbonReportMappingViews(items: MutableList<StrictCarbonReportMapping>): MutableList<StrictCarbonReportMappingView> {
    val views = mutableListOf<StrictCarbonReportMappingView>()
    for (item in items) {
        views.add(toStrictCarbonReportMappingView(item))
    }
    return views
}

fun sumStrictCarbonReportMappingView(view: StrictCarbonReportMappingView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedEndsAt.toDouble() + parsedCeiling
}
