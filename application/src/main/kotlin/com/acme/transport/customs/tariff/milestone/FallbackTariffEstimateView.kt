package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback tariff estimate. */
data class FallbackTariffEstimateView(
    var codeText: String,
    var title: String,
    var phone: String,
    var ownerId: String,
    var activeText: String,
    var version: String,
    var segment: String,
    var revision: String,
    var pinned: String,
    var capacity: String,
    var driverId: String,
    var statusLabel: String,
)

fun toFallbackTariffEstimateView(item: FallbackTariffEstimate): FallbackTariffEstimateView {
    return FallbackTariffEstimateView(
        codeText = truncate(item.code, 80),
        title = item.title,
        phone = item.phone,
        ownerId = item.ownerId,
        activeText = "${item.active}",
        version = item.version.toString(),
        segment = item.segment,
        revision = item.revision.toString(),
        pinned = "${item.pinned}",
        capacity = item.capacity.toString(),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTariffEstimateViews(items: MutableList<FallbackTariffEstimate>): MutableList<FallbackTariffEstimateView> {
    val views = mutableListOf<FallbackTariffEstimateView>()
    for (item in items) {
        views.add(toFallbackTariffEstimateView(item))
    }
    return views
}

fun sumFallbackTariffEstimateView(view: FallbackTariffEstimateView): Double {
    val parsedVersion = view.version.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedVersion.toDouble() + parsedRevision.toDouble() + parsedCapacity.toDouble()
}
