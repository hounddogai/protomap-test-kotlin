package com.acme.transport.fleetops.defect.recorditem

/** How screens and exports show a defect variance. */
data class DefectVarianceView(
    var code: String,
    var labelText: String,
    var category: String,
    var checksum: String,
    var stopCount: String,
    var versionText: String,
    var description: String,
    var statusLabel: String,
)

fun toDefectVarianceView(item: DefectVariance): DefectVarianceView {
    return DefectVarianceView(
        code = item.code,
        labelText = item.label,
        category = item.category,
        checksum = item.checksum,
        stopCount = item.stopCount.toString(),
        versionText = item.version.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectVarianceViews(items: MutableList<DefectVariance>): MutableList<DefectVarianceView> {
    val views = mutableListOf<DefectVarianceView>()
    for (item in items) {
        views.add(toDefectVarianceView(item))
    }
    return views
}

fun sumDefectVarianceView(view: DefectVarianceView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedStopCount.toDouble() + parsedVersion.toDouble()
}
