package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted service plan manifest. */
data class WeightedServicePlanManifestView(
    var code: String,
    var title: String,
    var startsAt: String,
    var externalRef: String,
    var pallets: String,
    var active: String,
    var visible: String,
    var approved: String,
    var marginText: String,
    var etaMinutes: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toWeightedServicePlanManifestView(item: WeightedServicePlanManifest): WeightedServicePlanManifestView {
    return WeightedServicePlanManifestView(
        code = item.code,
        title = truncate(item.title, 80),
        startsAt = item.startsAt.toString(),
        externalRef = truncate(item.externalRef, 80),
        pallets = item.pallets.toString(),
        active = "${item.active}",
        visible = "${item.visible}",
        approved = "${item.approved}",
        marginText = "%.2f".format(item.margin),
        etaMinutes = item.etaMinutes.toString(),
        reviewerText = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedServicePlanManifestViews(items: MutableList<WeightedServicePlanManifest>): MutableList<WeightedServicePlanManifestView> {
    val views = mutableListOf<WeightedServicePlanManifestView>()
    for (item in items) {
        views.add(toWeightedServicePlanManifestView(item))
    }
    return views
}

fun sumWeightedServicePlanManifestView(view: WeightedServicePlanManifestView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedStartsAt.toDouble() + parsedPallets.toDouble() + parsedMargin
}
