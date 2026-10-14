package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a camera event variance. */
data class CameraEventVarianceView(
    var codeText: String,
    var name: String,
    var revision: String,
    var unitText: String,
    var archived: String,
    var reviewer: String,
    var budgetText: String,
    var regionText: String,
    var statusLabel: String,
)

fun toCameraEventVarianceView(item: CameraEventVariance): CameraEventVarianceView {
    return CameraEventVarianceView(
        codeText = truncate(item.code, 80),
        name = item.name,
        revision = item.revision.toString(),
        unitText = item.unit,
        archived = "${item.archived}",
        reviewer = item.reviewer,
        budgetText = "%.2f".format(item.budget),
        regionText = truncate(item.region, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventVarianceViews(items: MutableList<CameraEventVariance>): MutableList<CameraEventVarianceView> {
    val views = mutableListOf<CameraEventVarianceView>()
    for (item in items) {
        views.add(toCameraEventVarianceView(item))
    }
    return views
}

fun sumCameraEventVarianceView(view: CameraEventVarianceView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedRevision.toDouble() + parsedBudget
}
