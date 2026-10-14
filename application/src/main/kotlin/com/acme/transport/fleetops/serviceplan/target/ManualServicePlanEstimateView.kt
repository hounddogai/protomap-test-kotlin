package com.acme.transport.fleetops.serviceplan.target

/** How screens and exports show a manual service plan estimate. */
data class ManualServicePlanEstimateView(
    var code: String,
    var title: String,
    var notes: String,
    var graceDaysText: String,
    var pinned: String,
    var description: String,
    var revision: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toManualServicePlanEstimateView(item: ManualServicePlanEstimate): ManualServicePlanEstimateView {
    return ManualServicePlanEstimateView(
        code = item.code,
        title = item.title,
        notes = item.notes,
        graceDaysText = item.graceDays.toString(),
        pinned = "${item.pinned}",
        description = item.description,
        revision = item.revision.toString(),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualServicePlanEstimateViews(items: MutableList<ManualServicePlanEstimate>): MutableList<ManualServicePlanEstimateView> {
    val views = mutableListOf<ManualServicePlanEstimateView>()
    for (item in items) {
        views.add(toManualServicePlanEstimateView(item))
    }
    return views
}

fun sumManualServicePlanEstimateView(view: ManualServicePlanEstimateView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedGraceDays.toDouble() + parsedRevision.toDouble()
}
