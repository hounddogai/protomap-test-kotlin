package com.acme.transport.lastmile.attempt.allocation

/** How screens and exports show a attempt adjustment. */
data class AttemptAdjustmentView(
    var codeText: String,
    var label: String,
    var channel: String,
    var ceiling: String,
    var routeCode: String,
    var quantityText: String,
    var sourceText: String,
    var stopCount: String,
    var ownerId: String,
    var amountText: String,
    var statusLabel: String,
)

fun toAttemptAdjustmentView(item: AttemptAdjustment): AttemptAdjustmentView {
    return AttemptAdjustmentView(
        codeText = item.code,
        label = item.label,
        channel = item.channel,
        ceiling = "%.2f".format(item.ceiling),
        routeCode = item.routeCode,
        quantityText = item.quantity.toString(),
        sourceText = item.source,
        stopCount = item.stopCount.toString(),
        ownerId = item.ownerId,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptAdjustmentViews(items: MutableList<AttemptAdjustment>): MutableList<AttemptAdjustmentView> {
    val views = mutableListOf<AttemptAdjustmentView>()
    for (item in items) {
        views.add(toAttemptAdjustmentView(item))
    }
    return views
}

fun sumAttemptAdjustmentView(view: AttemptAdjustmentView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedCeiling + parsedQuantity.toDouble() + parsedStopCount.toDouble()
}
