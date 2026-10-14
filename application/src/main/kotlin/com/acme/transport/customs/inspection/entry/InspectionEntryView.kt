package com.acme.transport.customs.inspection.entry

/** How screens and exports show a inspection entry. */
data class InspectionEntryView(
    var code: String,
    var title: String,
    var contactEmail: String,
    var nameText: String,
    var attempts: String,
    var stopCount: String,
    var etaMinutesText: String,
    var localeText: String,
    var revisionText: String,
    var trailerId: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toInspectionEntryView(item: InspectionEntry): InspectionEntryView {
    return InspectionEntryView(
        code = item.code,
        title = item.title,
        contactEmail = item.contactEmail,
        nameText = item.name,
        attempts = item.attempts.toString(),
        stopCount = item.stopCount.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        localeText = item.locale,
        revisionText = item.revision.toString(),
        trailerId = item.trailerId,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionEntryViews(items: MutableList<InspectionEntry>): MutableList<InspectionEntryView> {
    val views = mutableListOf<InspectionEntryView>()
    for (item in items) {
        views.add(toInspectionEntryView(item))
    }
    return views
}

fun sumInspectionEntryView(view: InspectionEntryView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedAttempts.toDouble() + parsedStopCount.toDouble() + parsedEtaMinutes.toDouble()
}
