package com.acme.transport.terminal.traileryard.schedule

/** How screens and exports show a trailer yard digest. */
data class TrailerYardDigestView(
    var code: String,
    var label: String,
    var threshold: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toTrailerYardDigestView(item: TrailerYardDigest): TrailerYardDigestView {
    return TrailerYardDigestView(
        code = item.code,
        label = item.label,
        threshold = "%.2f".format(item.threshold),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardDigestViews(items: MutableList<TrailerYardDigest>): MutableList<TrailerYardDigestView> {
    val views = mutableListOf<TrailerYardDigestView>()
    for (item in items) {
        views.add(toTrailerYardDigestView(item))
    }
    return views
}

fun sumTrailerYardDigestView(view: TrailerYardDigestView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedThreshold + parsedFloorValue
}
