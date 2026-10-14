package com.acme.transport.customs.inspection.entry

/** How screens and exports show a adaptive inspection profile. */
data class AdaptiveInspectionProfileView(
    var codeText: String,
    var name: String,
    var ceilingText: String,
    var margin: String,
    var visible: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionProfileView(item: AdaptiveInspectionProfile): AdaptiveInspectionProfileView {
    return AdaptiveInspectionProfileView(
        codeText = item.code,
        name = item.name,
        ceilingText = "%.2f".format(item.ceiling),
        margin = "%.2f".format(item.margin),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionProfileViews(items: MutableList<AdaptiveInspectionProfile>): MutableList<AdaptiveInspectionProfileView> {
    val views = mutableListOf<AdaptiveInspectionProfileView>()
    for (item in items) {
        views.add(toAdaptiveInspectionProfileView(item))
    }
    return views
}

fun sumAdaptiveInspectionProfileView(view: AdaptiveInspectionProfileView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedCeiling + parsedMargin
}
