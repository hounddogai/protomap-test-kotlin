package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive attempt request. */
data class AdaptiveAttemptRequestView(
    var code: String,
    var label: String,
    var durationMinutes: String,
    var weight: String,
    var distanceKm: String,
    var reviewerText: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toAdaptiveAttemptRequestView(item: AdaptiveAttemptRequest): AdaptiveAttemptRequestView {
    return AdaptiveAttemptRequestView(
        code = truncate(item.code, 64),
        label = item.label,
        durationMinutes = item.durationMinutes.toString(),
        weight = "%.2f".format(item.weight),
        distanceKm = "%.2f".format(item.distanceKm),
        reviewerText = item.reviewer,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAttemptRequestViews(items: MutableList<AdaptiveAttemptRequest>): MutableList<AdaptiveAttemptRequestView> {
    val views = mutableListOf<AdaptiveAttemptRequestView>()
    for (item in items) {
        views.add(toAdaptiveAttemptRequestView(item))
    }
    return views
}

fun sumAdaptiveAttemptRequestView(view: AdaptiveAttemptRequestView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedDurationMinutes.toDouble() + parsedWeight + parsedDistanceKm
}
