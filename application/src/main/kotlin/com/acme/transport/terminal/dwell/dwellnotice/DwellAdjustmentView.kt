package com.acme.transport.terminal.dwell.dwellnotice

/** How screens and exports show a dwell adjustment. */
data class DwellAdjustmentView(
    var code: String,
    var name: String,
    var threshold: String,
    var summary: String,
    var priority: String,
    var statusLabel: String,
)

fun toDwellAdjustmentView(item: DwellAdjustment): DwellAdjustmentView {
    return DwellAdjustmentView(
        code = item.code,
        name = item.name,
        threshold = "%.2f".format(item.threshold),
        summary = item.summary,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellAdjustmentViews(items: MutableList<DwellAdjustment>): MutableList<DwellAdjustmentView> {
    val views = mutableListOf<DwellAdjustmentView>()
    for (item in items) {
        views.add(toDwellAdjustmentView(item))
    }
    return views
}

fun sumDwellAdjustmentView(view: DwellAdjustmentView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedThreshold + parsedPriority.toDouble()
}
