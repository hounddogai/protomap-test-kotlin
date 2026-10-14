package com.acme.transport.fleetops.defect.policy

/** How screens and exports show a regional defect mapping. */
data class RegionalDefectMappingView(
    var code: String,
    var name: String,
    var timezoneText: String,
    var shiftCode: String,
    var quantity: String,
    var statusLabel: String,
)

fun toRegionalDefectMappingView(item: RegionalDefectMapping): RegionalDefectMappingView {
    return RegionalDefectMappingView(
        code = item.code,
        name = item.name,
        timezoneText = item.timezone,
        shiftCode = item.shiftCode,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDefectMappingViews(items: MutableList<RegionalDefectMapping>): MutableList<RegionalDefectMappingView> {
    val views = mutableListOf<RegionalDefectMappingView>()
    for (item in items) {
        views.add(toRegionalDefectMappingView(item))
    }
    return views
}

fun sumRegionalDefectMappingView(view: RegionalDefectMappingView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
