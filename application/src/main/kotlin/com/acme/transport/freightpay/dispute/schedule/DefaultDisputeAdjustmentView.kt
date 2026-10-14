package com.acme.transport.freightpay.dispute.schedule

/** How screens and exports show a default dispute adjustment. */
data class DefaultDisputeAdjustmentView(
    var code: String,
    var name: String,
    var margin: String,
    var minimum: String,
    var maximum: String,
    var dock: String,
    var categoryText: String,
    var revision: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toDefaultDisputeAdjustmentView(item: DefaultDisputeAdjustment): DefaultDisputeAdjustmentView {
    return DefaultDisputeAdjustmentView(
        code = item.code,
        name = item.name,
        margin = "%.2f".format(item.margin),
        minimum = "%.2f".format(item.minimum),
        maximum = "%.2f".format(item.maximum),
        dock = item.dock,
        categoryText = item.category,
        revision = item.revision.toString(),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDisputeAdjustmentViews(items: MutableList<DefaultDisputeAdjustment>): MutableList<DefaultDisputeAdjustmentView> {
    val views = mutableListOf<DefaultDisputeAdjustmentView>()
    for (item in items) {
        views.add(toDefaultDisputeAdjustmentView(item))
    }
    return views
}

fun sumDefaultDisputeAdjustmentView(view: DefaultDisputeAdjustmentView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedMargin + parsedMinimum + parsedMaximum
}
