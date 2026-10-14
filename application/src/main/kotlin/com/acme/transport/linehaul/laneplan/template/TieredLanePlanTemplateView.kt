package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered lane plan template. */
data class TieredLanePlanTemplateView(
    var code: String,
    var name: String,
    var fuelLiters: String,
    var archived: String,
    var sequence: String,
    var reviewer: String,
    var title: String,
    var vehicleIdText: String,
    var attempts: String,
    var amount: String,
    var checksum: String,
    var statusLabel: String,
)

fun toTieredLanePlanTemplateView(item: TieredLanePlanTemplate): TieredLanePlanTemplateView {
    return TieredLanePlanTemplateView(
        code = item.code,
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        archived = "${item.archived}",
        sequence = item.sequence.toString(),
        reviewer = item.reviewer,
        title = item.title,
        vehicleIdText = truncate(item.vehicleId, 80),
        attempts = item.attempts.toString(),
        amount = "%.2f".format(item.amount),
        checksum = truncate(item.checksum, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredLanePlanTemplateViews(items: MutableList<TieredLanePlanTemplate>): MutableList<TieredLanePlanTemplateView> {
    val views = mutableListOf<TieredLanePlanTemplateView>()
    for (item in items) {
        views.add(toTieredLanePlanTemplateView(item))
    }
    return views
}

fun sumTieredLanePlanTemplateView(view: TieredLanePlanTemplateView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedFuelLiters + parsedSequence.toDouble() + parsedAttempts.toDouble()
}
