package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop digest. */
data class DropDigestView(
    var codeText: String,
    var label: String,
    var retries: String,
    var weight: String,
    var visible: String,
    var statusLabel: String,
)

fun toDropDigestView(item: DropDigest): DropDigestView {
    return DropDigestView(
        codeText = truncate(item.code, 120),
        label = item.label,
        retries = item.retries.toString(),
        weight = "%.2f".format(item.weight),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropDigestViews(items: MutableList<DropDigest>): MutableList<DropDigestView> {
    val views = mutableListOf<DropDigestView>()
    for (item in items) {
        views.add(toDropDigestView(item))
    }
    return views
}

fun sumDropDigestView(view: DropDigestView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedRetries.toDouble() + parsedWeight
}
