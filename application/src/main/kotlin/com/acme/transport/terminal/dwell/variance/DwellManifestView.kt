package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell manifest. */
data class DwellManifestView(
    var code: String,
    var title: String,
    var sequenceText: String,
    var country: String,
    var budgetText: String,
    var active: String,
    var name: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toDwellManifestView(item: DwellManifest): DwellManifestView {
    return DwellManifestView(
        code = truncate(item.code, 80),
        title = item.title,
        sequenceText = item.sequence.toString(),
        country = item.country,
        budgetText = "%.2f".format(item.budget),
        active = "${item.active}",
        name = item.name,
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellManifestViews(items: MutableList<DwellManifest>): MutableList<DwellManifestView> {
    val views = mutableListOf<DwellManifestView>()
    for (item in items) {
        views.add(toDwellManifestView(item))
    }
    return views
}

fun sumDwellManifestView(view: DwellManifestView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedSequence.toDouble() + parsedBudget
}
