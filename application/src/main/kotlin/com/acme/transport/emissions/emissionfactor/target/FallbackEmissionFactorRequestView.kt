package com.acme.transport.emissions.emissionfactor.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback emission factor request. */
data class FallbackEmissionFactorRequestView(
    var code: String,
    var titleText: String,
    var vehicleIdText: String,
    var phoneText: String,
    var version: String,
    var discount: String,
    var durationMinutes: String,
    var summaryText: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toFallbackEmissionFactorRequestView(item: FallbackEmissionFactorRequest): FallbackEmissionFactorRequestView {
    return FallbackEmissionFactorRequestView(
        code = item.code,
        titleText = item.title,
        vehicleIdText = item.vehicleId,
        phoneText = item.phone,
        version = item.version.toString(),
        discount = "%.2f".format(item.discount),
        durationMinutes = item.durationMinutes.toString(),
        summaryText = item.summary,
        driverIdText = truncate(item.driverId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackEmissionFactorRequestViews(items: MutableList<FallbackEmissionFactorRequest>): MutableList<FallbackEmissionFactorRequestView> {
    val views = mutableListOf<FallbackEmissionFactorRequestView>()
    for (item in items) {
        views.add(toFallbackEmissionFactorRequestView(item))
    }
    return views
}

fun sumFallbackEmissionFactorRequestView(view: FallbackEmissionFactorRequestView): Double {
    val parsedVersion = view.version.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedVersion.toDouble() + parsedDiscount + parsedDurationMinutes.toDouble()
}
