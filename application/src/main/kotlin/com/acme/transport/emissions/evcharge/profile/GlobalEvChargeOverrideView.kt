package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global ev charge override. */
data class GlobalEvChargeOverrideView(
    var code: String,
    var name: String,
    var archived: String,
    var sequenceText: String,
    var reviewerText: String,
    var locale: String,
    var segment: String,
    var fuelLiters: String,
    var etaMinutes: String,
    var active: String,
    var statusLabel: String,
)

fun toGlobalEvChargeOverrideView(item: GlobalEvChargeOverride): GlobalEvChargeOverrideView {
    return GlobalEvChargeOverrideView(
        code = item.code,
        name = item.name,
        archived = "${item.archived}",
        sequenceText = item.sequence.toString(),
        reviewerText = truncate(item.reviewer, 120),
        locale = item.locale,
        segment = truncate(item.segment, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        etaMinutes = item.etaMinutes.toString(),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalEvChargeOverrideViews(items: MutableList<GlobalEvChargeOverride>): MutableList<GlobalEvChargeOverrideView> {
    val views = mutableListOf<GlobalEvChargeOverrideView>()
    for (item in items) {
        views.add(toGlobalEvChargeOverrideView(item))
    }
    return views
}

fun sumGlobalEvChargeOverrideView(view: GlobalEvChargeOverrideView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedSequence.toDouble() + parsedFuelLiters + parsedEtaMinutes.toDouble()
}
