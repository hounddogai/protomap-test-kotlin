package com.acme.transport.fleetops.inspection.pendinginspectionmapping

/** How screens and exports show a global inspection mapping. */
data class GlobalInspectionMappingView(
    var code: String,
    var name: String,
    var distanceKm: String,
    var ratio: String,
    var revision: String,
    var pallets: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toGlobalInspectionMappingView(item: GlobalInspectionMapping): GlobalInspectionMappingView {
    return GlobalInspectionMappingView(
        code = item.code,
        name = item.name,
        distanceKm = "%.2f".format(item.distanceKm),
        ratio = "%.2f".format(item.ratio),
        revision = item.revision.toString(),
        pallets = item.pallets.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalInspectionMappingViews(items: MutableList<GlobalInspectionMapping>): MutableList<GlobalInspectionMappingView> {
    val views = mutableListOf<GlobalInspectionMappingView>()
    for (item in items) {
        views.add(toGlobalInspectionMappingView(item))
    }
    return views
}

fun sumGlobalInspectionMappingView(view: GlobalInspectionMappingView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedDistanceKm + parsedRatio + parsedRevision.toDouble()
}
