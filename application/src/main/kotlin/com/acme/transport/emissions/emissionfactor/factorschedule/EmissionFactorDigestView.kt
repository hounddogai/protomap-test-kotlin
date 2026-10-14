package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor digest. */
data class EmissionFactorDigestView(
    var code: String,
    var title: String,
    var label: String,
    var archived: String,
    var version: String,
    var pinned: String,
    var source: String,
    var summary: String,
    var category: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toEmissionFactorDigestView(item: EmissionFactorDigest): EmissionFactorDigestView {
    return EmissionFactorDigestView(
        code = truncate(item.code, 40),
        title = truncate(item.title, 64),
        label = item.label,
        archived = "${item.archived}",
        version = item.version.toString(),
        pinned = "${item.pinned}",
        source = item.source,
        summary = item.summary,
        category = item.category,
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorDigestViews(items: MutableList<EmissionFactorDigest>): MutableList<EmissionFactorDigestView> {
    val views = mutableListOf<EmissionFactorDigestView>()
    for (item in items) {
        views.add(toEmissionFactorDigestView(item))
    }
    return views
}

fun sumEmissionFactorDigestView(view: EmissionFactorDigestView): Double {
    val parsedVersion = view.version.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedVersion.toDouble() + parsedMinimum
}
