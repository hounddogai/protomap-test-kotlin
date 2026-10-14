package com.acme.transport.network.cutoff.batch

/** How screens and exports show a strict cutoff rollup. */
data class StrictCutoffRollupView(
    var code: String,
    var label: String,
    var maximum: String,
    var floorValue: String,
    var depot: String,
    var version: String,
    var statusLabel: String,
)

fun toStrictCutoffRollupView(item: StrictCutoffRollup): StrictCutoffRollupView {
    return StrictCutoffRollupView(
        code = item.code,
        label = item.label,
        maximum = "%.2f".format(item.maximum),
        floorValue = "%.2f".format(item.floorValue),
        depot = item.depot,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCutoffRollupViews(items: MutableList<StrictCutoffRollup>): MutableList<StrictCutoffRollupView> {
    val views = mutableListOf<StrictCutoffRollupView>()
    for (item in items) {
        views.add(toStrictCutoffRollupView(item))
    }
    return views
}

fun sumStrictCutoffRollupView(view: StrictCutoffRollupView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedMaximum + parsedFloorValue + parsedVersion.toDouble()
}
