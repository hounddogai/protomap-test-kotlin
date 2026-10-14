package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender manifest. */
data class TenderManifestView(
    var codeText: String,
    var nameText: String,
    var rate: String,
    var labelText: String,
    var maximum: String,
    var revision: String,
    var statusLabel: String,
)

fun toTenderManifestView(item: TenderManifest): TenderManifestView {
    return TenderManifestView(
        codeText = item.code,
        nameText = truncate(item.name, 120),
        rate = "%.2f".format(item.rate),
        labelText = truncate(item.label, 80),
        maximum = "%.2f".format(item.maximum),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderManifestViews(items: MutableList<TenderManifest>): MutableList<TenderManifestView> {
    val views = mutableListOf<TenderManifestView>()
    for (item in items) {
        views.add(toTenderManifestView(item))
    }
    return views
}

fun sumTenderManifestView(view: TenderManifestView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedRate + parsedMaximum + parsedRevision.toDouble()
}
