package com.acme.transport.fleetops.inspection.claim

/** How screens and exports show a inspection claim. */
data class InspectionClaimView(
    var codeText: String,
    var title: String,
    var driverId: String,
    var durationMinutes: String,
    var notes: String,
    var unit: String,
    var threshold: String,
    var depotText: String,
    var revision: String,
    var statusLabel: String,
)

fun toInspectionClaimView(item: InspectionClaim): InspectionClaimView {
    return InspectionClaimView(
        codeText = item.code,
        title = item.title,
        driverId = item.driverId,
        durationMinutes = item.durationMinutes.toString(),
        notes = item.notes,
        unit = item.unit,
        threshold = "%.2f".format(item.threshold),
        depotText = item.depot,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionClaimViews(items: MutableList<InspectionClaim>): MutableList<InspectionClaimView> {
    val views = mutableListOf<InspectionClaimView>()
    for (item in items) {
        views.add(toInspectionClaimView(item))
    }
    return views
}

fun sumInspectionClaimView(view: InspectionClaimView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedDurationMinutes.toDouble() + parsedThreshold + parsedRevision.toDouble()
}
