package com.acme.transport.customs.declaration.declarationsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal declaration assignment. */
data class SeasonalDeclarationAssignmentView(
    var code: String,
    var name: String,
    var odometer: String,
    var contactEmail: String,
    var batchSizeText: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toSeasonalDeclarationAssignmentView(item: SeasonalDeclarationAssignment): SeasonalDeclarationAssignmentView {
    return SeasonalDeclarationAssignmentView(
        code = item.code,
        name = item.name,
        odometer = item.odometer.toString(),
        contactEmail = truncate(item.contactEmail, 80),
        batchSizeText = item.batchSize.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDeclarationAssignmentViews(items: MutableList<SeasonalDeclarationAssignment>): MutableList<SeasonalDeclarationAssignmentView> {
    val views = mutableListOf<SeasonalDeclarationAssignmentView>()
    for (item in items) {
        views.add(toSeasonalDeclarationAssignmentView(item))
    }
    return views
}

fun sumSeasonalDeclarationAssignmentView(view: SeasonalDeclarationAssignmentView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedOdometer.toDouble() + parsedBatchSize.toDouble() + parsedWindowDays.toDouble()
}
