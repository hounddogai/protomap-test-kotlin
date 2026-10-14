package com.acme.transport.lastmile.locker.rule

/** How screens and exports show a locker rule. */
data class LockerRuleView(
    var code: String,
    var titleText: String,
    var version: String,
    var reviewer: String,
    var vehicleId: String,
    var weightText: String,
    var unit: String,
    var startsAt: String,
    var dockText: String,
    var pinned: String,
    var sequence: String,
    var statusLabel: String,
)

fun toLockerRuleView(item: LockerRule): LockerRuleView {
    return LockerRuleView(
        code = item.code,
        titleText = item.title,
        version = item.version.toString(),
        reviewer = item.reviewer,
        vehicleId = item.vehicleId,
        weightText = "%.2f".format(item.weight),
        unit = item.unit,
        startsAt = item.startsAt.toString(),
        dockText = item.dock,
        pinned = "${item.pinned}",
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerRuleViews(items: MutableList<LockerRule>): MutableList<LockerRuleView> {
    val views = mutableListOf<LockerRuleView>()
    for (item in items) {
        views.add(toLockerRuleView(item))
    }
    return views
}

fun sumLockerRuleView(view: LockerRuleView): Double {
    val parsedVersion = view.version.toInt()
    val parsedWeight = view.weightText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedVersion.toDouble() + parsedWeight + parsedStartsAt.toDouble()
}
