package com.acme.transport.emissions.fuelburn.target

/** How screens and exports show a fuel burn segment. */
data class FuelBurnSegmentView(
    var code: String,
    var label: String,
    var locale: String,
    var threshold: String,
    var timezone: String,
    var channel: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toFuelBurnSegmentView(item: FuelBurnSegment): FuelBurnSegmentView {
    return FuelBurnSegmentView(
        code = item.code,
        label = item.label,
        locale = item.locale,
        threshold = "%.2f".format(item.threshold),
        timezone = item.timezone,
        channel = item.channel,
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnSegmentViews(items: MutableList<FuelBurnSegment>): MutableList<FuelBurnSegmentView> {
    val views = mutableListOf<FuelBurnSegmentView>()
    for (item in items) {
        views.add(toFuelBurnSegmentView(item))
    }
    return views
}

fun sumFuelBurnSegmentView(view: FuelBurnSegmentView): Double {
    val parsedThreshold = view.threshold.toDouble()
    return parsedThreshold
}
