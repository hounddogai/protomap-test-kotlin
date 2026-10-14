package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted locker profile. */
data class WeightedLockerProfileView(
    var code: String,
    var title: String,
    var segment: String,
    var dock: String,
    var stopCount: String,
    var label: String,
    var floorValue: String,
    var budget: String,
    var minimum: String,
    var statusLabel: String,
)

fun toWeightedLockerProfileView(item: WeightedLockerProfile): WeightedLockerProfileView {
    return WeightedLockerProfileView(
        code = item.code,
        title = truncate(item.title, 40),
        segment = truncate(item.segment, 80),
        dock = item.dock,
        stopCount = item.stopCount.toString(),
        label = item.label,
        floorValue = "%.2f".format(item.floorValue),
        budget = "%.2f".format(item.budget),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedLockerProfileViews(items: MutableList<WeightedLockerProfile>): MutableList<WeightedLockerProfileView> {
    val views = mutableListOf<WeightedLockerProfileView>()
    for (item in items) {
        views.add(toWeightedLockerProfileView(item))
    }
    return views
}

fun sumWeightedLockerProfileView(view: WeightedLockerProfileView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedStopCount.toDouble() + parsedFloorValue + parsedBudget
}
