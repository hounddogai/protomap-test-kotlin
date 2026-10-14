package com.acme.transport.network.servicearea.batch

/** How screens and exports show a tiered service area batch. */
data class TieredServiceAreaBatchView(
    var codeText: String,
    var title: String,
    var description: String,
    var countryText: String,
    var activeText: String,
    var timezoneText: String,
    var maximum: String,
    var floorValueText: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toTieredServiceAreaBatchView(item: TieredServiceAreaBatch): TieredServiceAreaBatchView {
    return TieredServiceAreaBatchView(
        codeText = item.code,
        title = item.title,
        description = item.description,
        countryText = item.country,
        activeText = "${item.active}",
        timezoneText = item.timezone,
        maximum = "%.2f".format(item.maximum),
        floorValueText = "%.2f".format(item.floorValue),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredServiceAreaBatchViews(items: MutableList<TieredServiceAreaBatch>): MutableList<TieredServiceAreaBatchView> {
    val views = mutableListOf<TieredServiceAreaBatchView>()
    for (item in items) {
        views.add(toTieredServiceAreaBatchView(item))
    }
    return views
}

fun sumTieredServiceAreaBatchView(view: TieredServiceAreaBatchView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedMaximum + parsedFloorValue
}
