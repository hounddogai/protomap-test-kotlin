package com.acme.transport.lastmile.drop.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop bundle. */
data class DropBundleView(
    var codeText: String,
    var labelText: String,
    var durationMinutes: String,
    var sequence: String,
    var description: String,
    var discount: String,
    var revisionText: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toDropBundleView(item: DropBundle): DropBundleView {
    return DropBundleView(
        codeText = item.code,
        labelText = item.label,
        durationMinutes = item.durationMinutes.toString(),
        sequence = item.sequence.toString(),
        description = item.description,
        discount = "%.2f".format(item.discount),
        revisionText = item.revision.toString(),
        externalRefText = truncate(item.externalRef, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropBundleViews(items: MutableList<DropBundle>): MutableList<DropBundleView> {
    val views = mutableListOf<DropBundleView>()
    for (item in items) {
        views.add(toDropBundleView(item))
    }
    return views
}

fun sumDropBundleView(view: DropBundleView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedDurationMinutes.toDouble() + parsedSequence.toDouble() + parsedDiscount
}
