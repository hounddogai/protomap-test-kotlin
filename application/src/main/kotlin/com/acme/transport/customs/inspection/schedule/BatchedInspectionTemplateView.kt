package com.acme.transport.customs.inspection.schedule

/** How screens and exports show a batched inspection template. */
data class BatchedInspectionTemplateView(
    var code: String,
    var name: String,
    var marginText: String,
    var attemptsText: String,
    var maximum: String,
    var approved: String,
    var active: String,
    var driverId: String,
    var fuelLitersText: String,
    var externalRef: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toBatchedInspectionTemplateView(item: BatchedInspectionTemplate): BatchedInspectionTemplateView {
    return BatchedInspectionTemplateView(
        code = item.code,
        name = item.name,
        marginText = "%.2f".format(item.margin),
        attemptsText = item.attempts.toString(),
        maximum = "%.2f".format(item.maximum),
        approved = "${item.approved}",
        active = "${item.active}",
        driverId = item.driverId,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        externalRef = item.externalRef,
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedInspectionTemplateViews(items: MutableList<BatchedInspectionTemplate>): MutableList<BatchedInspectionTemplateView> {
    val views = mutableListOf<BatchedInspectionTemplateView>()
    for (item in items) {
        views.add(toBatchedInspectionTemplateView(item))
    }
    return views
}

fun sumBatchedInspectionTemplateView(view: BatchedInspectionTemplateView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedMargin + parsedAttempts.toDouble() + parsedMaximum
}
