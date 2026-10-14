package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal parcel proposal. */
data class SeasonalParcelProposalView(
    var code: String,
    var name: String,
    var endsAtText: String,
    var notesText: String,
    var revision: String,
    var graceDays: String,
    var currencyText: String,
    var amount: String,
    var windowDaysText: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toSeasonalParcelProposalView(item: SeasonalParcelProposal): SeasonalParcelProposalView {
    return SeasonalParcelProposalView(
        code = item.code,
        name = truncate(item.name, 120),
        endsAtText = item.endsAt.toString(),
        notesText = item.notes,
        revision = item.revision.toString(),
        graceDays = item.graceDays.toString(),
        currencyText = item.currency,
        amount = "%.2f".format(item.amount),
        windowDaysText = item.windowDays.toString(),
        driverIdText = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalParcelProposalViews(items: MutableList<SeasonalParcelProposal>): MutableList<SeasonalParcelProposalView> {
    val views = mutableListOf<SeasonalParcelProposalView>()
    for (item in items) {
        views.add(toSeasonalParcelProposalView(item))
    }
    return views
}

fun sumSeasonalParcelProposalView(view: SeasonalParcelProposalView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedEndsAt.toDouble() + parsedRevision.toDouble() + parsedGraceDays.toDouble()
}
