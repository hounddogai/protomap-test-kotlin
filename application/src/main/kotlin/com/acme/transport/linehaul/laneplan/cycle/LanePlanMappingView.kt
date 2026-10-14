package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan mapping. */
data class LanePlanMappingView(
    var code: String,
    var label: String,
    var country: String,
    var checksum: String,
    var budget: String,
    var vehicleIdText: String,
    var revision: String,
    var sequenceText: String,
    var odometer: String,
    var contactEmail: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toLanePlanMappingView(item: LanePlanMapping): LanePlanMappingView {
    return LanePlanMappingView(
        code = item.code,
        label = item.label,
        country = truncate(item.country, 80),
        checksum = truncate(item.checksum, 64),
        budget = "%.2f".format(item.budget),
        vehicleIdText = item.vehicleId,
        revision = item.revision.toString(),
        sequenceText = item.sequence.toString(),
        odometer = item.odometer.toString(),
        contactEmail = item.contactEmail,
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanMappingViews(items: MutableList<LanePlanMapping>): MutableList<LanePlanMappingView> {
    val views = mutableListOf<LanePlanMappingView>()
    for (item in items) {
        views.add(toLanePlanMappingView(item))
    }
    return views
}

fun sumLanePlanMappingView(view: LanePlanMappingView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedBudget + parsedRevision.toDouble() + parsedSequence.toDouble()
}
