package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global fuel burn schedule. */
data class GlobalFuelBurnScheduleView(
    var code: String,
    var titleText: String,
    var country: String,
    var regionText: String,
    var floorValue: String,
    var updatedAt: String,
    var summaryText: String,
    var discount: String,
    var category: String,
    var currency: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toGlobalFuelBurnScheduleView(item: GlobalFuelBurnSchedule): GlobalFuelBurnScheduleView {
    return GlobalFuelBurnScheduleView(
        code = truncate(item.code, 64),
        titleText = item.title,
        country = item.country,
        regionText = item.region,
        floorValue = "%.2f".format(item.floorValue),
        updatedAt = item.updatedAt.toString(),
        summaryText = item.summary,
        discount = "%.2f".format(item.discount),
        category = item.category,
        currency = truncate(item.currency, 120),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalFuelBurnScheduleViews(items: MutableList<GlobalFuelBurnSchedule>): MutableList<GlobalFuelBurnScheduleView> {
    val views = mutableListOf<GlobalFuelBurnScheduleView>()
    for (item in items) {
        views.add(toGlobalFuelBurnScheduleView(item))
    }
    return views
}

fun sumGlobalFuelBurnScheduleView(view: GlobalFuelBurnScheduleView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedFloorValue + parsedUpdatedAt.toDouble() + parsedDiscount
}
