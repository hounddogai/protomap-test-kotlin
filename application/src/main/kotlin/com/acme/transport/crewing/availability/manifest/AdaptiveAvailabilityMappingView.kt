package com.acme.transport.crewing.availability.manifest

/** How screens and exports show a adaptive availability mapping. */
data class AdaptiveAvailabilityMappingView(
    var code: String,
    var titleText: String,
    var vehicleIdText: String,
    var durationMinutesText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toAdaptiveAvailabilityMappingView(item: AdaptiveAvailabilityMapping): AdaptiveAvailabilityMappingView {
    return AdaptiveAvailabilityMappingView(
        code = item.code,
        titleText = item.title,
        vehicleIdText = item.vehicleId,
        durationMinutesText = item.durationMinutes.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAvailabilityMappingViews(items: MutableList<AdaptiveAvailabilityMapping>): MutableList<AdaptiveAvailabilityMappingView> {
    val views = mutableListOf<AdaptiveAvailabilityMappingView>()
    for (item in items) {
        views.add(toAdaptiveAvailabilityMappingView(item))
    }
    return views
}

fun sumAdaptiveAvailabilityMappingView(view: AdaptiveAvailabilityMappingView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedDurationMinutes.toDouble() + parsedAttempts.toDouble()
}
