package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive near miss exception. */
data class AdaptiveNearMissExceptionView(
    var code: String,
    var name: String,
    var sequence: String,
    var category: String,
    var capacity: String,
    var reviewer: String,
    var loadKg: String,
    var odometer: String,
    var trailerIdText: String,
    var distanceKmText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toAdaptiveNearMissExceptionView(item: AdaptiveNearMissException): AdaptiveNearMissExceptionView {
    return AdaptiveNearMissExceptionView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 64),
        sequence = item.sequence.toString(),
        category = item.category,
        capacity = item.capacity.toString(),
        reviewer = truncate(item.reviewer, 80),
        loadKg = "%.2f".format(item.loadKg),
        odometer = item.odometer.toString(),
        trailerIdText = item.trailerId,
        distanceKmText = "%.2f".format(item.distanceKm),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveNearMissExceptionViews(items: MutableList<AdaptiveNearMissException>): MutableList<AdaptiveNearMissExceptionView> {
    val views = mutableListOf<AdaptiveNearMissExceptionView>()
    for (item in items) {
        views.add(toAdaptiveNearMissExceptionView(item))
    }
    return views
}

fun sumAdaptiveNearMissExceptionView(view: AdaptiveNearMissExceptionView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedSequence.toDouble() + parsedCapacity.toDouble() + parsedLoadKg
}
