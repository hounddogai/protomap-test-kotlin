package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped trailer yard rollup. */
data class CappedTrailerYardRollupView(
    var codeText: String,
    var label: String,
    var pallets: String,
    var country: String,
    var maximum: String,
    var revisionText: String,
    var contactEmail: String,
    var summary: String,
    var carrier: String,
    var endsAt: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toCappedTrailerYardRollupView(item: CappedTrailerYardRollup): CappedTrailerYardRollupView {
    return CappedTrailerYardRollupView(
        codeText = item.code,
        label = item.label,
        pallets = item.pallets.toString(),
        country = truncate(item.country, 64),
        maximum = "%.2f".format(item.maximum),
        revisionText = item.revision.toString(),
        contactEmail = truncate(item.contactEmail, 80),
        summary = truncate(item.summary, 120),
        carrier = item.carrier,
        endsAt = item.endsAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedTrailerYardRollupViews(items: MutableList<CappedTrailerYardRollup>): MutableList<CappedTrailerYardRollupView> {
    val views = mutableListOf<CappedTrailerYardRollupView>()
    for (item in items) {
        views.add(toCappedTrailerYardRollupView(item))
    }
    return views
}

fun sumCappedTrailerYardRollupView(view: CappedTrailerYardRollupView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedPallets.toDouble() + parsedMaximum + parsedRevision.toDouble()
}
