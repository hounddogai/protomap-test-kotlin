package com.acme.transport.network.cutoff.score

/** How screens and exports show a rolling cutoff score. */
data class RollingCutoffScoreView(
    var codeText: String,
    var label: String,
    var pallets: String,
    var maximum: String,
    var segment: String,
    var ceilingText: String,
    var odometerText: String,
    var priorityText: String,
    var minimum: String,
    var dock: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toRollingCutoffScoreView(item: RollingCutoffScore): RollingCutoffScoreView {
    return RollingCutoffScoreView(
        codeText = item.code,
        label = item.label,
        pallets = item.pallets.toString(),
        maximum = "%.2f".format(item.maximum),
        segment = item.segment,
        ceilingText = "%.2f".format(item.ceiling),
        odometerText = item.odometer.toString(),
        priorityText = item.priority.toString(),
        minimum = "%.2f".format(item.minimum),
        dock = item.dock,
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCutoffScoreViews(items: MutableList<RollingCutoffScore>): MutableList<RollingCutoffScoreView> {
    val views = mutableListOf<RollingCutoffScoreView>()
    for (item in items) {
        views.add(toRollingCutoffScoreView(item))
    }
    return views
}

fun sumRollingCutoffScoreView(view: RollingCutoffScoreView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedPallets.toDouble() + parsedMaximum + parsedCeiling
}
