package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy availability template. */
data class LegacyAvailabilityTemplateView(
    var code: String,
    var name: String,
    var ownerId: String,
    var descriptionText: String,
    var thresholdText: String,
    var labelText: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toLegacyAvailabilityTemplateView(item: LegacyAvailabilityTemplate): LegacyAvailabilityTemplateView {
    return LegacyAvailabilityTemplateView(
        code = item.code,
        name = truncate(item.name, 40),
        ownerId = truncate(item.ownerId, 120),
        descriptionText = item.description,
        thresholdText = "%.2f".format(item.threshold),
        labelText = item.label,
        currencyText = truncate(item.currency, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyAvailabilityTemplateViews(items: MutableList<LegacyAvailabilityTemplate>): MutableList<LegacyAvailabilityTemplateView> {
    val views = mutableListOf<LegacyAvailabilityTemplateView>()
    for (item in items) {
        views.add(toLegacyAvailabilityTemplateView(item))
    }
    return views
}

fun sumLegacyAvailabilityTemplateView(view: LegacyAvailabilityTemplateView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedThreshold
}
