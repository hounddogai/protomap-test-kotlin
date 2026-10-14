package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary drop entry. */
data class PrimaryDropEntryView(
    var code: String,
    var titleText: String,
    var approvedText: String,
    var contactEmail: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toPrimaryDropEntryView(item: PrimaryDropEntry): PrimaryDropEntryView {
    return PrimaryDropEntryView(
        code = item.code,
        titleText = item.title,
        approvedText = "${item.approved}",
        contactEmail = truncate(item.contactEmail, 120),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDropEntryViews(items: MutableList<PrimaryDropEntry>): MutableList<PrimaryDropEntryView> {
    val views = mutableListOf<PrimaryDropEntryView>()
    for (item in items) {
        views.add(toPrimaryDropEntryView(item))
    }
    return views
}

fun sumPrimaryDropEntryView(view: PrimaryDropEntryView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
