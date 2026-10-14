package com.acme.transport.emissions.evcharge.chargebaseline

/** How screens and exports show a strict ev charge baseline. */
data class StrictEvChargeBaselineView(
    var code: String,
    var title: String,
    var pinned: String,
    var dockText: String,
    var revisionText: String,
    var currency: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toStrictEvChargeBaselineView(item: StrictEvChargeBaseline): StrictEvChargeBaselineView {
    return StrictEvChargeBaselineView(
        code = item.code,
        title = item.title,
        pinned = "${item.pinned}",
        dockText = item.dock,
        revisionText = item.revision.toString(),
        currency = item.currency,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictEvChargeBaselineViews(items: MutableList<StrictEvChargeBaseline>): MutableList<StrictEvChargeBaselineView> {
    val views = mutableListOf<StrictEvChargeBaselineView>()
    for (item in items) {
        views.add(toStrictEvChargeBaselineView(item))
    }
    return views
}

fun sumStrictEvChargeBaselineView(view: StrictEvChargeBaselineView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedRevision.toDouble() + parsedEndsAt.toDouble()
}
