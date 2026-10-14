package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive service plan manifest. */
data class AdaptiveServicePlanManifestView(
    var code: String,
    var title: String,
    var attempts: String,
    var categoryText: String,
    var marginText: String,
    var externalRef: String,
    var segment: String,
    var ownerId: String,
    var pallets: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toAdaptiveServicePlanManifestView(item: AdaptiveServicePlanManifest): AdaptiveServicePlanManifestView {
    return AdaptiveServicePlanManifestView(
        code = item.code,
        title = item.title,
        attempts = item.attempts.toString(),
        categoryText = item.category,
        marginText = "%.2f".format(item.margin),
        externalRef = item.externalRef,
        segment = item.segment,
        ownerId = truncate(item.ownerId, 40),
        pallets = item.pallets.toString(),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveServicePlanManifestViews(items: MutableList<AdaptiveServicePlanManifest>): MutableList<AdaptiveServicePlanManifestView> {
    val views = mutableListOf<AdaptiveServicePlanManifestView>()
    for (item in items) {
        views.add(toAdaptiveServicePlanManifestView(item))
    }
    return views
}

fun sumAdaptiveServicePlanManifestView(view: AdaptiveServicePlanManifestView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedAttempts.toDouble() + parsedMargin + parsedPallets.toDouble()
}
