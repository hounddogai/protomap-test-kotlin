package com.acme.transport.network.cutoff.segment

/** How screens and exports show a cutoff segment. */
data class CutoffSegmentView(
    var code: String,
    var nameText: String,
    var summaryText: String,
    var category: String,
    var endsAt: String,
    var expiresAtText: String,
    var visible: String,
    var vehicleIdText: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toCutoffSegmentView(item: CutoffSegment): CutoffSegmentView {
    return CutoffSegmentView(
        code = item.code,
        nameText = item.name,
        summaryText = item.summary,
        category = item.category,
        endsAt = item.endsAt.toString(),
        expiresAtText = item.expiresAt.toString(),
        visible = "${item.visible}",
        vehicleIdText = item.vehicleId,
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffSegmentViews(items: MutableList<CutoffSegment>): MutableList<CutoffSegmentView> {
    val views = mutableListOf<CutoffSegmentView>()
    for (item in items) {
        views.add(toCutoffSegmentView(item))
    }
    return views
}

fun sumCutoffSegmentView(view: CutoffSegmentView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedEndsAt.toDouble() + parsedExpiresAt.toDouble() + parsedWindowDays.toDouble()
}
