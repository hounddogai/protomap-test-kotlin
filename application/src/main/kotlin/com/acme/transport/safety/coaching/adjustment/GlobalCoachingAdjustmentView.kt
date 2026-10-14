package com.acme.transport.safety.coaching.adjustment

/** How screens and exports show a global coaching adjustment. */
data class GlobalCoachingAdjustmentView(
    var codeText: String,
    var titleText: String,
    var shiftCodeText: String,
    var rate: String,
    var floorValue: String,
    var endsAt: String,
    var timezone: String,
    var region: String,
    var statusLabel: String,
)

fun toGlobalCoachingAdjustmentView(item: GlobalCoachingAdjustment): GlobalCoachingAdjustmentView {
    return GlobalCoachingAdjustmentView(
        codeText = item.code,
        titleText = item.title,
        shiftCodeText = item.shiftCode,
        rate = "%.2f".format(item.rate),
        floorValue = "%.2f".format(item.floorValue),
        endsAt = item.endsAt.toString(),
        timezone = item.timezone,
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCoachingAdjustmentViews(items: MutableList<GlobalCoachingAdjustment>): MutableList<GlobalCoachingAdjustmentView> {
    val views = mutableListOf<GlobalCoachingAdjustmentView>()
    for (item in items) {
        views.add(toGlobalCoachingAdjustmentView(item))
    }
    return views
}

fun sumGlobalCoachingAdjustmentView(view: GlobalCoachingAdjustmentView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedRate + parsedFloorValue + parsedEndsAt.toDouble()
}
