package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default emission factor adjustment. */
data class DefaultEmissionFactorAdjustmentView(
    var code: String,
    var nameText: String,
    var startsAtText: String,
    var distanceKm: String,
    var revisionText: String,
    var trailerId: String,
    var vehicleId: String,
    var minimum: String,
    var statusLabel: String,
)

fun toDefaultEmissionFactorAdjustmentView(item: DefaultEmissionFactorAdjustment): DefaultEmissionFactorAdjustmentView {
    return DefaultEmissionFactorAdjustmentView(
        code = item.code,
        nameText = truncate(item.name, 120),
        startsAtText = item.startsAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        revisionText = item.revision.toString(),
        trailerId = item.trailerId,
        vehicleId = item.vehicleId,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultEmissionFactorAdjustmentViews(items: MutableList<DefaultEmissionFactorAdjustment>): MutableList<DefaultEmissionFactorAdjustmentView> {
    val views = mutableListOf<DefaultEmissionFactorAdjustmentView>()
    for (item in items) {
        views.add(toDefaultEmissionFactorAdjustmentView(item))
    }
    return views
}

fun sumDefaultEmissionFactorAdjustmentView(view: DefaultEmissionFactorAdjustmentView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedStartsAt.toDouble() + parsedDistanceKm + parsedRevision.toDouble()
}
