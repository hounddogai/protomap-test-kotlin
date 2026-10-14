package com.acme.transport.emissions.emissionfactor.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default emission factor segment. */
data class DefaultEmissionFactorSegmentView(
    var code: String,
    var labelText: String,
    var endsAt: String,
    var loadKg: String,
    var odometer: String,
    var nameText: String,
    var statusLabel: String,
)

fun toDefaultEmissionFactorSegmentView(item: DefaultEmissionFactorSegment): DefaultEmissionFactorSegmentView {
    return DefaultEmissionFactorSegmentView(
        code = item.code,
        labelText = truncate(item.label, 64),
        endsAt = item.endsAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        odometer = item.odometer.toString(),
        nameText = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultEmissionFactorSegmentViews(items: MutableList<DefaultEmissionFactorSegment>): MutableList<DefaultEmissionFactorSegmentView> {
    val views = mutableListOf<DefaultEmissionFactorSegmentView>()
    for (item in items) {
        views.add(toDefaultEmissionFactorSegmentView(item))
    }
    return views
}

fun sumDefaultEmissionFactorSegmentView(view: DefaultEmissionFactorSegmentView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedEndsAt.toDouble() + parsedLoadKg + parsedOdometer.toDouble()
}
