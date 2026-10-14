package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a parcel template. */
data class ParcelTemplateView(
    var codeText: String,
    var nameText: String,
    var ceiling: String,
    var pallets: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toParcelTemplateView(item: ParcelTemplate): ParcelTemplateView {
    return ParcelTemplateView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        ceiling = "%.2f".format(item.ceiling),
        pallets = item.pallets.toString(),
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelTemplateViews(items: MutableList<ParcelTemplate>): MutableList<ParcelTemplateView> {
    val views = mutableListOf<ParcelTemplateView>()
    for (item in items) {
        views.add(toParcelTemplateView(item))
    }
    return views
}

fun sumParcelTemplateView(view: ParcelTemplateView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedCeiling + parsedPallets.toDouble() + parsedSequence.toDouble()
}
