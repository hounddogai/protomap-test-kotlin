package com.acme.transport.emissions.idling.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default idling entry. */
data class DefaultIdlingEntryView(
    var code: String,
    var nameText: String,
    var checksumText: String,
    var palletsText: String,
    var ratio: String,
    var approvedText: String,
    var etaMinutesText: String,
    var source: String,
    var revision: String,
    var reviewer: String,
    var discount: String,
    var statusLabel: String,
)

fun toDefaultIdlingEntryView(item: DefaultIdlingEntry): DefaultIdlingEntryView {
    return DefaultIdlingEntryView(
        code = item.code,
        nameText = item.name,
        checksumText = item.checksum,
        palletsText = item.pallets.toString(),
        ratio = "%.2f".format(item.ratio),
        approvedText = "${item.approved}",
        etaMinutesText = item.etaMinutes.toString(),
        source = truncate(item.source, 40),
        revision = item.revision.toString(),
        reviewer = item.reviewer,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultIdlingEntryViews(items: MutableList<DefaultIdlingEntry>): MutableList<DefaultIdlingEntryView> {
    val views = mutableListOf<DefaultIdlingEntryView>()
    for (item in items) {
        views.add(toDefaultIdlingEntryView(item))
    }
    return views
}

fun sumDefaultIdlingEntryView(view: DefaultIdlingEntryView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedPallets.toDouble() + parsedRatio + parsedEtaMinutes.toDouble()
}
