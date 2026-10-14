package com.acme.transport.network.cutoff.adjustment

/** How screens and exports show a cutoff record. */
data class CutoffRecordView(
    var code: String,
    var label: String,
    var margin: String,
    var pinned: String,
    var distanceKm: String,
    var threshold: String,
    var limit: String,
    var statusLabel: String,
)

fun toCutoffRecordView(item: CutoffRecord): CutoffRecordView {
    return CutoffRecordView(
        code = item.code,
        label = item.label,
        margin = "%.2f".format(item.margin),
        pinned = "${item.pinned}",
        distanceKm = "%.2f".format(item.distanceKm),
        threshold = "%.2f".format(item.threshold),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffRecordViews(items: MutableList<CutoffRecord>): MutableList<CutoffRecordView> {
    val views = mutableListOf<CutoffRecordView>()
    for (item in items) {
        views.add(toCutoffRecordView(item))
    }
    return views
}

fun sumCutoffRecordView(view: CutoffRecordView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedMargin + parsedDistanceKm + parsedThreshold
}
