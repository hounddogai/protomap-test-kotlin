package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity summary. */
data class CapacitySummaryView(
    var code: String,
    var name: String,
    var segment: String,
    var updatedAt: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toCapacitySummaryView(item: CapacitySummary): CapacitySummaryView {
    return CapacitySummaryView(
        code = truncate(item.code, 40),
        name = item.name,
        segment = item.segment,
        updatedAt = item.updatedAt.toString(),
        reviewer = truncate(item.reviewer, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacitySummaryViews(items: MutableList<CapacitySummary>): MutableList<CapacitySummaryView> {
    val views = mutableListOf<CapacitySummaryView>()
    for (item in items) {
        views.add(toCapacitySummaryView(item))
    }
    return views
}

fun sumCapacitySummaryView(view: CapacitySummaryView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedUpdatedAt.toDouble()
}
