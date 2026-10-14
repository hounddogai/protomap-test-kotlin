package com.acme.transport.fleetops.inspection.quota

/** How screens and exports show a adaptive inspection audit. */
data class AdaptiveInspectionAuditView(
    var code: String,
    var name: String,
    var minimum: String,
    var unit: String,
    var shiftCode: String,
    var maximumText: String,
    var loadKg: String,
    var fuelLiters: String,
    var notes: String,
    var locale: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionAuditView(item: AdaptiveInspectionAudit): AdaptiveInspectionAuditView {
    return AdaptiveInspectionAuditView(
        code = item.code,
        name = item.name,
        minimum = "%.2f".format(item.minimum),
        unit = item.unit,
        shiftCode = item.shiftCode,
        maximumText = "%.2f".format(item.maximum),
        loadKg = "%.2f".format(item.loadKg),
        fuelLiters = "%.2f".format(item.fuelLiters),
        notes = item.notes,
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionAuditViews(items: MutableList<AdaptiveInspectionAudit>): MutableList<AdaptiveInspectionAuditView> {
    val views = mutableListOf<AdaptiveInspectionAuditView>()
    for (item in items) {
        views.add(toAdaptiveInspectionAuditView(item))
    }
    return views
}

fun sumAdaptiveInspectionAuditView(view: AdaptiveInspectionAuditView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedMinimum + parsedMaximum + parsedLoadKg
}
