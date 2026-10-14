package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection template. */
data class InspectionTemplateView(
    var code: String,
    var name: String,
    var countryText: String,
    var dock: String,
    var capacity: String,
    var reviewer: String,
    var retries: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toInspectionTemplateView(item: InspectionTemplate): InspectionTemplateView {
    return InspectionTemplateView(
        code = truncate(item.code, 80),
        name = item.name,
        countryText = truncate(item.country, 120),
        dock = item.dock,
        capacity = item.capacity.toString(),
        reviewer = item.reviewer,
        retries = item.retries.toString(),
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionTemplateViews(items: MutableList<InspectionTemplate>): MutableList<InspectionTemplateView> {
    val views = mutableListOf<InspectionTemplateView>()
    for (item in items) {
        views.add(toInspectionTemplateView(item))
    }
    return views
}

fun sumInspectionTemplateView(view: InspectionTemplateView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedCapacity.toDouble() + parsedRetries.toDouble() + parsedExpiresAt.toDouble()
}
