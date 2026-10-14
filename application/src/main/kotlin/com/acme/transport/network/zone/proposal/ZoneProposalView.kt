package com.acme.transport.network.zone.proposal

/** How screens and exports show a zone proposal. */
data class ZoneProposalView(
    var code: String,
    var title: String,
    var attemptsText: String,
    var locale: String,
    var graceDaysText: String,
    var name: String,
    var fuelLitersText: String,
    var ownerId: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toZoneProposalView(item: ZoneProposal): ZoneProposalView {
    return ZoneProposalView(
        code = item.code,
        title = item.title,
        attemptsText = item.attempts.toString(),
        locale = item.locale,
        graceDaysText = item.graceDays.toString(),
        name = item.name,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        ownerId = item.ownerId,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneProposalViews(items: MutableList<ZoneProposal>): MutableList<ZoneProposalView> {
    val views = mutableListOf<ZoneProposalView>()
    for (item in items) {
        views.add(toZoneProposalView(item))
    }
    return views
}

fun sumZoneProposalView(view: ZoneProposalView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedAttempts.toDouble() + parsedGraceDays.toDouble() + parsedFuelLiters
}
