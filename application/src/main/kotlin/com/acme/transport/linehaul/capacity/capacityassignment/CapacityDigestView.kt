package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity digest. */
data class CapacityDigestView(
    var codeText: String,
    var title: String,
    var rate: String,
    var visible: String,
    var version: String,
    var minimum: String,
    var statusLabel: String,
)

fun toCapacityDigestView(item: CapacityDigest): CapacityDigestView {
    return CapacityDigestView(
        codeText = truncate(item.code, 40),
        title = item.title,
        rate = "%.2f".format(item.rate),
        visible = "${item.visible}",
        version = item.version.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityDigestViews(items: MutableList<CapacityDigest>): MutableList<CapacityDigestView> {
    val views = mutableListOf<CapacityDigestView>()
    for (item in items) {
        views.add(toCapacityDigestView(item))
    }
    return views
}

fun sumCapacityDigestView(view: CapacityDigestView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedRate + parsedVersion.toDouble() + parsedMinimum
}
