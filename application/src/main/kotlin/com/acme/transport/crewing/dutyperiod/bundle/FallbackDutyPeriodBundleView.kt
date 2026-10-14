package com.acme.transport.crewing.dutyperiod.bundle

/** How screens and exports show a fallback duty period bundle. */
data class FallbackDutyPeriodBundleView(
    var code: String,
    var labelText: String,
    var floorValue: String,
    var carrier: String,
    var phoneText: String,
    var shiftCode: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toFallbackDutyPeriodBundleView(item: FallbackDutyPeriodBundle): FallbackDutyPeriodBundleView {
    return FallbackDutyPeriodBundleView(
        code = item.code,
        labelText = item.label,
        floorValue = "%.2f".format(item.floorValue),
        carrier = item.carrier,
        phoneText = item.phone,
        shiftCode = item.shiftCode,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDutyPeriodBundleViews(items: MutableList<FallbackDutyPeriodBundle>): MutableList<FallbackDutyPeriodBundleView> {
    val views = mutableListOf<FallbackDutyPeriodBundleView>()
    for (item in items) {
        views.add(toFallbackDutyPeriodBundleView(item))
    }
    return views
}

fun sumFallbackDutyPeriodBundleView(view: FallbackDutyPeriodBundleView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedFloorValue + parsedWindowDays.toDouble()
}
