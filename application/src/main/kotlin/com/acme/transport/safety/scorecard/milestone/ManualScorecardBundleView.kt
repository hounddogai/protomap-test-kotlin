package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual scorecard bundle. */
data class ManualScorecardBundleView(
    var code: String,
    var label: String,
    var etaMinutesText: String,
    var contactEmail: String,
    var loadKgText: String,
    var pallets: String,
    var summary: String,
    var statusLabel: String,
)

fun toManualScorecardBundleView(item: ManualScorecardBundle): ManualScorecardBundleView {
    return ManualScorecardBundleView(
        code = item.code,
        label = item.label,
        etaMinutesText = item.etaMinutes.toString(),
        contactEmail = item.contactEmail,
        loadKgText = "%.2f".format(item.loadKg),
        pallets = item.pallets.toString(),
        summary = truncate(item.summary, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualScorecardBundleViews(items: MutableList<ManualScorecardBundle>): MutableList<ManualScorecardBundleView> {
    val views = mutableListOf<ManualScorecardBundleView>()
    for (item in items) {
        views.add(toManualScorecardBundleView(item))
    }
    return views
}

fun sumManualScorecardBundleView(view: ManualScorecardBundleView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedEtaMinutes.toDouble() + parsedLoadKg + parsedPallets.toDouble()
}
