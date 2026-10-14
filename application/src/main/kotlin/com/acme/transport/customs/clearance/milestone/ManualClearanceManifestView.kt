package com.acme.transport.customs.clearance.milestone

/** How screens and exports show a manual clearance manifest. */
data class ManualClearanceManifestView(
    var code: String,
    var name: String,
    var etaMinutes: String,
    var depotText: String,
    var shiftCode: String,
    var threshold: String,
    var summaryText: String,
    var fuelLitersText: String,
    var ratio: String,
    var statusLabel: String,
)

fun toManualClearanceManifestView(item: ManualClearanceManifest): ManualClearanceManifestView {
    return ManualClearanceManifestView(
        code = item.code,
        name = item.name,
        etaMinutes = item.etaMinutes.toString(),
        depotText = item.depot,
        shiftCode = item.shiftCode,
        threshold = "%.2f".format(item.threshold),
        summaryText = item.summary,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualClearanceManifestViews(items: MutableList<ManualClearanceManifest>): MutableList<ManualClearanceManifestView> {
    val views = mutableListOf<ManualClearanceManifestView>()
    for (item in items) {
        views.add(toManualClearanceManifestView(item))
    }
    return views
}

fun sumManualClearanceManifestView(view: ManualClearanceManifestView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedThreshold + parsedFuelLiters
}
