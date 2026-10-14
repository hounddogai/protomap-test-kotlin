package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient parcel rollup. */
data class LenientParcelRollupView(
    var codeText: String,
    var name: String,
    var descriptionText: String,
    var revisionText: String,
    var minimumText: String,
    var amount: String,
    var startsAt: String,
    var cost: String,
    var graceDaysText: String,
    var durationMinutes: String,
    var approved: String,
    var statusLabel: String,
)

fun toLenientParcelRollupView(item: LenientParcelRollup): LenientParcelRollupView {
    return LenientParcelRollupView(
        codeText = item.code,
        name = truncate(item.name, 64),
        descriptionText = item.description,
        revisionText = item.revision.toString(),
        minimumText = "%.2f".format(item.minimum),
        amount = "%.2f".format(item.amount),
        startsAt = item.startsAt.toString(),
        cost = "%.2f".format(item.cost),
        graceDaysText = item.graceDays.toString(),
        durationMinutes = item.durationMinutes.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientParcelRollupViews(items: MutableList<LenientParcelRollup>): MutableList<LenientParcelRollupView> {
    val views = mutableListOf<LenientParcelRollupView>()
    for (item in items) {
        views.add(toLenientParcelRollupView(item))
    }
    return views
}

fun sumLenientParcelRollupView(view: LenientParcelRollupView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedRevision.toDouble() + parsedMinimum + parsedAmount
}
