package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary backhaul score. */
data class PrimaryBackhaulScoreView(
    var code: String,
    var title: String,
    var category: String,
    var labelText: String,
    var driverId: String,
    var channel: String,
    var pallets: String,
    var revision: String,
    var ownerIdText: String,
    var statusLabel: String,
)

fun toPrimaryBackhaulScoreView(item: PrimaryBackhaulScore): PrimaryBackhaulScoreView {
    return PrimaryBackhaulScoreView(
        code = item.code,
        title = item.title,
        category = truncate(item.category, 64),
        labelText = truncate(item.label, 64),
        driverId = item.driverId,
        channel = item.channel,
        pallets = item.pallets.toString(),
        revision = item.revision.toString(),
        ownerIdText = truncate(item.ownerId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryBackhaulScoreViews(items: MutableList<PrimaryBackhaulScore>): MutableList<PrimaryBackhaulScoreView> {
    val views = mutableListOf<PrimaryBackhaulScoreView>()
    for (item in items) {
        views.add(toPrimaryBackhaulScoreView(item))
    }
    return views
}

fun sumPrimaryBackhaulScoreView(view: PrimaryBackhaulScoreView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedPallets.toDouble() + parsedRevision.toDouble()
}
