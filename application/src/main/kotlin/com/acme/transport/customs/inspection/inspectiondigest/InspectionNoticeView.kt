package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection notice. */
data class InspectionNoticeView(
    var code: String,
    var nameText: String,
    var source: String,
    var country: String,
    var unit: String,
    var score: String,
    var statusLabel: String,
)

fun toInspectionNoticeView(item: InspectionNotice): InspectionNoticeView {
    return InspectionNoticeView(
        code = truncate(item.code, 80),
        nameText = item.name,
        source = truncate(item.source, 64),
        country = item.country,
        unit = item.unit,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionNoticeViews(items: MutableList<InspectionNotice>): MutableList<InspectionNoticeView> {
    val views = mutableListOf<InspectionNoticeView>()
    for (item in items) {
        views.add(toInspectionNoticeView(item))
    }
    return views
}

fun sumInspectionNoticeView(view: InspectionNoticeView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
