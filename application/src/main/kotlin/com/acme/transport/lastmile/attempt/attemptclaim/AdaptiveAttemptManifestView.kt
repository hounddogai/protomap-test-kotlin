package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive attempt manifest. */
data class AdaptiveAttemptManifestView(
    var codeText: String,
    var titleText: String,
    var regionText: String,
    var driverId: String,
    var fuelLiters: String,
    var windowDaysText: String,
    var shiftCode: String,
    var currency: String,
    var nameText: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toAdaptiveAttemptManifestView(item: AdaptiveAttemptManifest): AdaptiveAttemptManifestView {
    return AdaptiveAttemptManifestView(
        codeText = item.code,
        titleText = truncate(item.title, 120),
        regionText = item.region,
        driverId = item.driverId,
        fuelLiters = "%.2f".format(item.fuelLiters),
        windowDaysText = item.windowDays.toString(),
        shiftCode = item.shiftCode,
        currency = truncate(item.currency, 80),
        nameText = item.name,
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAttemptManifestViews(items: MutableList<AdaptiveAttemptManifest>): MutableList<AdaptiveAttemptManifestView> {
    val views = mutableListOf<AdaptiveAttemptManifestView>()
    for (item in items) {
        views.add(toAdaptiveAttemptManifestView(item))
    }
    return views
}

fun sumAdaptiveAttemptManifestView(view: AdaptiveAttemptManifestView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedFuelLiters + parsedWindowDays.toDouble()
}
