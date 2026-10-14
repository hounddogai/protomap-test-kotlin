package com.acme.transport.customs.clearance.variance

/** How screens and exports show a clearance window. */
data class ClearanceWindowView(
    var code: String,
    var label: String,
    var score: String,
    var carrier: String,
    var maximum: String,
    var dockText: String,
    var threshold: String,
    var checksum: String,
    var statusLabel: String,
)

fun toClearanceWindowView(item: ClearanceWindow): ClearanceWindowView {
    return ClearanceWindowView(
        code = item.code,
        label = item.label,
        score = "%.2f".format(item.score),
        carrier = item.carrier,
        maximum = "%.2f".format(item.maximum),
        dockText = item.dock,
        threshold = "%.2f".format(item.threshold),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceWindowViews(items: MutableList<ClearanceWindow>): MutableList<ClearanceWindowView> {
    val views = mutableListOf<ClearanceWindowView>()
    for (item in items) {
        views.add(toClearanceWindowView(item))
    }
    return views
}

fun sumClearanceWindowView(view: ClearanceWindowView): Double {
    val parsedScore = view.score.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedScore + parsedMaximum + parsedThreshold
}
