package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global trailer proposal. */
data class GlobalTrailerProposalView(
    var code: String,
    var name: String,
    var fuelLiters: String,
    var sequence: String,
    var timezoneText: String,
    var expiresAt: String,
    var createdAtText: String,
    var durationMinutesText: String,
    var titleText: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toGlobalTrailerProposalView(item: GlobalTrailerProposal): GlobalTrailerProposalView {
    return GlobalTrailerProposalView(
        code = truncate(item.code, 40),
        name = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        sequence = item.sequence.toString(),
        timezoneText = item.timezone,
        expiresAt = item.expiresAt.toString(),
        createdAtText = item.createdAt.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        titleText = item.title,
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalTrailerProposalViews(items: MutableList<GlobalTrailerProposal>): MutableList<GlobalTrailerProposalView> {
    val views = mutableListOf<GlobalTrailerProposalView>()
    for (item in items) {
        views.add(toGlobalTrailerProposalView(item))
    }
    return views
}

fun sumGlobalTrailerProposalView(view: GlobalTrailerProposalView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedFuelLiters + parsedSequence.toDouble() + parsedExpiresAt.toDouble()
}
