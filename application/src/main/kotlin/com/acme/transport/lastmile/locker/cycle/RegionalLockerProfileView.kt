package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional locker profile. */
data class RegionalLockerProfileView(
    var code: String,
    var name: String,
    var description: String,
    var cost: String,
    var reviewer: String,
    var title: String,
    var fuelLiters: String,
    var timezoneText: String,
    var priority: String,
    var capacity: String,
    var statusLabel: String,
)

fun toRegionalLockerProfileView(item: RegionalLockerProfile): RegionalLockerProfileView {
    return RegionalLockerProfileView(
        code = item.code,
        name = item.name,
        description = item.description,
        cost = "%.2f".format(item.cost),
        reviewer = item.reviewer,
        title = truncate(item.title, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        timezoneText = truncate(item.timezone, 120),
        priority = item.priority.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalLockerProfileViews(items: MutableList<RegionalLockerProfile>): MutableList<RegionalLockerProfileView> {
    val views = mutableListOf<RegionalLockerProfileView>()
    for (item in items) {
        views.add(toRegionalLockerProfileView(item))
    }
    return views
}

fun sumRegionalLockerProfileView(view: RegionalLockerProfileView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedCost + parsedFuelLiters + parsedPriority.toDouble()
}
