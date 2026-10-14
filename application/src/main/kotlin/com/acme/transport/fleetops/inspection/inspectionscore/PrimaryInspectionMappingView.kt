package com.acme.transport.fleetops.inspection.inspectionscore

/** How screens and exports show a primary inspection mapping. */
data class PrimaryInspectionMappingView(
    var code: String,
    var label: String,
    var archived: String,
    var graceDays: String,
    var segment: String,
    var endsAt: String,
    var score: String,
    var weight: String,
    var externalRefText: String,
    var source: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toPrimaryInspectionMappingView(item: PrimaryInspectionMapping): PrimaryInspectionMappingView {
    return PrimaryInspectionMappingView(
        code = item.code,
        label = item.label,
        archived = "${item.archived}",
        graceDays = item.graceDays.toString(),
        segment = item.segment,
        endsAt = item.endsAt.toString(),
        score = "%.2f".format(item.score),
        weight = "%.2f".format(item.weight),
        externalRefText = item.externalRef,
        source = item.source,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryInspectionMappingViews(items: MutableList<PrimaryInspectionMapping>): MutableList<PrimaryInspectionMappingView> {
    val views = mutableListOf<PrimaryInspectionMappingView>()
    for (item in items) {
        views.add(toPrimaryInspectionMappingView(item))
    }
    return views
}

fun sumPrimaryInspectionMappingView(view: PrimaryInspectionMappingView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedScore = view.score.toDouble()
    return parsedGraceDays.toDouble() + parsedEndsAt.toDouble() + parsedScore
}
