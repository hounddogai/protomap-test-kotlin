package com.acme.transport.fleetops.defect.mapping

/** How screens and exports show a defect mapping. */
data class DefectMappingView(
    var code: String,
    var label: String,
    var endsAt: String,
    var fuelLiters: String,
    var revision: String,
    var approved: String,
    var notes: String,
    var pinnedText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toDefectMappingView(item: DefectMapping): DefectMappingView {
    return DefectMappingView(
        code = item.code,
        label = item.label,
        endsAt = item.endsAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        revision = item.revision.toString(),
        approved = "${item.approved}",
        notes = item.notes,
        pinnedText = "${item.pinned}",
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectMappingViews(items: MutableList<DefectMapping>): MutableList<DefectMappingView> {
    val views = mutableListOf<DefectMappingView>()
    for (item in items) {
        views.add(toDefectMappingView(item))
    }
    return views
}

fun sumDefectMappingView(view: DefectMappingView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedEndsAt.toDouble() + parsedFuelLiters + parsedRevision.toDouble()
}
