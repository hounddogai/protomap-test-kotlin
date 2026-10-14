package com.acme.transport.safety.violation.batch

/** How screens and exports show a capped violation batch. */
data class CappedViolationBatchView(
    var code: String,
    var labelText: String,
    var priority: String,
    var minimum: String,
    var floorValueText: String,
    var etaMinutes: String,
    var attempts: String,
    var statusLabel: String,
)

fun toCappedViolationBatchView(item: CappedViolationBatch): CappedViolationBatchView {
    return CappedViolationBatchView(
        code = item.code,
        labelText = item.label,
        priority = item.priority.toString(),
        minimum = "%.2f".format(item.minimum),
        floorValueText = "%.2f".format(item.floorValue),
        etaMinutes = item.etaMinutes.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedViolationBatchViews(items: MutableList<CappedViolationBatch>): MutableList<CappedViolationBatchView> {
    val views = mutableListOf<CappedViolationBatchView>()
    for (item in items) {
        views.add(toCappedViolationBatchView(item))
    }
    return views
}

fun sumCappedViolationBatchView(view: CappedViolationBatchView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedPriority.toDouble() + parsedMinimum + parsedFloorValue
}
