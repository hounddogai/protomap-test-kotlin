package com.acme.transport.fleetops.inspection.profile

/** How screens and exports show a weighted inspection baseline. */
data class WeightedInspectionBaselineView(
    var code: String,
    var labelText: String,
    var reviewerText: String,
    var loadKgText: String,
    var checksum: String,
    var locale: String,
    var statusLabel: String,
)

fun toWeightedInspectionBaselineView(item: WeightedInspectionBaseline): WeightedInspectionBaselineView {
    return WeightedInspectionBaselineView(
        code = item.code,
        labelText = item.label,
        reviewerText = item.reviewer,
        loadKgText = "%.2f".format(item.loadKg),
        checksum = item.checksum,
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedInspectionBaselineViews(items: MutableList<WeightedInspectionBaseline>): MutableList<WeightedInspectionBaselineView> {
    val views = mutableListOf<WeightedInspectionBaselineView>()
    for (item in items) {
        views.add(toWeightedInspectionBaselineView(item))
    }
    return views
}

fun sumWeightedInspectionBaselineView(view: WeightedInspectionBaselineView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedLoadKg
}
