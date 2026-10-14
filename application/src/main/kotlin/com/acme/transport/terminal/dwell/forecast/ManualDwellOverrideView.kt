package com.acme.transport.terminal.dwell.forecast

/** How screens and exports show a manual dwell override. */
data class ManualDwellOverrideView(
    var code: String,
    var name: String,
    var limitText: String,
    var floorValue: String,
    var discount: String,
    var pinnedText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toManualDwellOverrideView(item: ManualDwellOverride): ManualDwellOverrideView {
    return ManualDwellOverrideView(
        code = item.code,
        name = item.name,
        limitText = item.limit.toString(),
        floorValue = "%.2f".format(item.floorValue),
        discount = "%.2f".format(item.discount),
        pinnedText = "${item.pinned}",
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDwellOverrideViews(items: MutableList<ManualDwellOverride>): MutableList<ManualDwellOverrideView> {
    val views = mutableListOf<ManualDwellOverrideView>()
    for (item in items) {
        views.add(toManualDwellOverrideView(item))
    }
    return views
}

fun sumManualDwellOverrideView(view: ManualDwellOverrideView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedLimit.toDouble() + parsedFloorValue + parsedDiscount
}
