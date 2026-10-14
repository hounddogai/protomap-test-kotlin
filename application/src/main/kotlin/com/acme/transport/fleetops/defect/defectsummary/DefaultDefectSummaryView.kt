package com.acme.transport.fleetops.defect.defectsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default defect summary. */
data class DefaultDefectSummaryView(
    var code: String,
    var labelText: String,
    var rate: String,
    var pinned: String,
    var phone: String,
    var titleText: String,
    var statusLabel: String,
)

fun toDefaultDefectSummaryView(item: DefaultDefectSummary): DefaultDefectSummaryView {
    return DefaultDefectSummaryView(
        code = item.code,
        labelText = item.label,
        rate = "%.2f".format(item.rate),
        pinned = "${item.pinned}",
        phone = item.phone,
        titleText = truncate(item.title, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDefectSummaryViews(items: MutableList<DefaultDefectSummary>): MutableList<DefaultDefectSummaryView> {
    val views = mutableListOf<DefaultDefectSummaryView>()
    for (item in items) {
        views.add(toDefaultDefectSummaryView(item))
    }
    return views
}

fun sumDefaultDefectSummaryView(view: DefaultDefectSummaryView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
