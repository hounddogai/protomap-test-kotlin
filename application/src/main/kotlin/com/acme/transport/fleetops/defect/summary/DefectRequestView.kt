package com.acme.transport.fleetops.defect.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect request. */
data class DefectRequestView(
    var codeText: String,
    var name: String,
    var channel: String,
    var ownerIdText: String,
    var fuelLiters: String,
    var currency: String,
    var externalRefText: String,
    var archived: String,
    var sequenceText: String,
    var active: String,
    var statusLabel: String,
)

fun toDefectRequestView(item: DefectRequest): DefectRequestView {
    return DefectRequestView(
        codeText = item.code,
        name = truncate(item.name, 64),
        channel = truncate(item.channel, 120),
        ownerIdText = item.ownerId,
        fuelLiters = "%.2f".format(item.fuelLiters),
        currency = item.currency,
        externalRefText = item.externalRef,
        archived = "${item.archived}",
        sequenceText = item.sequence.toString(),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectRequestViews(items: MutableList<DefectRequest>): MutableList<DefectRequestView> {
    val views = mutableListOf<DefectRequestView>()
    for (item in items) {
        views.add(toDefectRequestView(item))
    }
    return views
}

fun sumDefectRequestView(view: DefectRequestView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedFuelLiters + parsedSequence.toDouble()
}
