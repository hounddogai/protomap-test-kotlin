package com.acme.transport.crewing.restbreak.adjustment

/** How screens and exports show a rest break adjustment. */
data class RestBreakAdjustmentView(
    var codeText: String,
    var label: String,
    var title: String,
    var palletsText: String,
    var ratio: String,
    var weightText: String,
    var priorityText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toRestBreakAdjustmentView(item: RestBreakAdjustment): RestBreakAdjustmentView {
    return RestBreakAdjustmentView(
        codeText = item.code,
        label = item.label,
        title = item.title,
        palletsText = item.pallets.toString(),
        ratio = "%.2f".format(item.ratio),
        weightText = "%.2f".format(item.weight),
        priorityText = item.priority.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakAdjustmentViews(items: MutableList<RestBreakAdjustment>): MutableList<RestBreakAdjustmentView> {
    val views = mutableListOf<RestBreakAdjustmentView>()
    for (item in items) {
        views.add(toRestBreakAdjustmentView(item))
    }
    return views
}

fun sumRestBreakAdjustmentView(view: RestBreakAdjustmentView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedPallets.toDouble() + parsedRatio + parsedWeight
}
