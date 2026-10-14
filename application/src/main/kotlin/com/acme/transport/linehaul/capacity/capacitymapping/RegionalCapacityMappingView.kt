package com.acme.transport.linehaul.capacity.capacitymapping

/** How screens and exports show a regional capacity mapping. */
data class RegionalCapacityMappingView(
    var code: String,
    var label: String,
    var timezone: String,
    var discount: String,
    var thresholdText: String,
    var source: String,
    var depotText: String,
    var statusLabel: String,
)

fun toRegionalCapacityMappingView(item: RegionalCapacityMapping): RegionalCapacityMappingView {
    return RegionalCapacityMappingView(
        code = item.code,
        label = item.label,
        timezone = item.timezone,
        discount = "%.2f".format(item.discount),
        thresholdText = "%.2f".format(item.threshold),
        source = item.source,
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCapacityMappingViews(items: MutableList<RegionalCapacityMapping>): MutableList<RegionalCapacityMappingView> {
    val views = mutableListOf<RegionalCapacityMappingView>()
    for (item in items) {
        views.add(toRegionalCapacityMappingView(item))
    }
    return views
}

fun sumRegionalCapacityMappingView(view: RegionalCapacityMappingView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedDiscount + parsedThreshold
}
