package com.acme.transport.customs.declaration.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration summary. */
data class DeclarationSummaryView(
    var code: String,
    var nameText: String,
    var stopCount: String,
    var labelText: String,
    var priority: String,
    var windowDaysText: String,
    var vehicleIdText: String,
    var updatedAt: String,
    var discountText: String,
    var margin: String,
    var statusLabel: String,
)

fun toDeclarationSummaryView(item: DeclarationSummary): DeclarationSummaryView {
    return DeclarationSummaryView(
        code = item.code,
        nameText = item.name,
        stopCount = item.stopCount.toString(),
        labelText = truncate(item.label, 80),
        priority = item.priority.toString(),
        windowDaysText = item.windowDays.toString(),
        vehicleIdText = item.vehicleId,
        updatedAt = item.updatedAt.toString(),
        discountText = "%.2f".format(item.discount),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationSummaryViews(items: MutableList<DeclarationSummary>): MutableList<DeclarationSummaryView> {
    val views = mutableListOf<DeclarationSummaryView>()
    for (item in items) {
        views.add(toDeclarationSummaryView(item))
    }
    return views
}

fun sumDeclarationSummaryView(view: DeclarationSummaryView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedStopCount.toDouble() + parsedPriority.toDouble() + parsedWindowDays.toDouble()
}
