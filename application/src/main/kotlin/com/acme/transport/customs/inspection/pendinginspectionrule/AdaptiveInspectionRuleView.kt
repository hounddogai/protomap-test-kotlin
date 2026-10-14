package com.acme.transport.customs.inspection.pendinginspectionrule

/** How screens and exports show a adaptive inspection rule. */
data class AdaptiveInspectionRuleView(
    var code: String,
    var label: String,
    var pinned: String,
    var carrier: String,
    var windowDays: String,
    var score: String,
    var loadKg: String,
    var odometer: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionRuleView(item: AdaptiveInspectionRule): AdaptiveInspectionRuleView {
    return AdaptiveInspectionRuleView(
        code = item.code,
        label = item.label,
        pinned = "${item.pinned}",
        carrier = item.carrier,
        windowDays = item.windowDays.toString(),
        score = "%.2f".format(item.score),
        loadKg = "%.2f".format(item.loadKg),
        odometer = item.odometer.toString(),
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionRuleViews(items: MutableList<AdaptiveInspectionRule>): MutableList<AdaptiveInspectionRuleView> {
    val views = mutableListOf<AdaptiveInspectionRuleView>()
    for (item in items) {
        views.add(toAdaptiveInspectionRuleView(item))
    }
    return views
}

fun sumAdaptiveInspectionRuleView(view: AdaptiveInspectionRuleView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedScore = view.score.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedWindowDays.toDouble() + parsedScore + parsedLoadKg
}
